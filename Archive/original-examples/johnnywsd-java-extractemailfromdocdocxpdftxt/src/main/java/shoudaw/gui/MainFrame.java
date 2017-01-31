package shoudaw.gui;


import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import shoudaw.file.MyFile;
import shoudaw.gui.file.FileChooser;
import shoudaw.msword.MsWordDoc;
import shoudaw.msword.MsWordDocx;
import shoudaw.pdf.SDWPdfReader;
import shoudaw.utils.GetEmail;

public class MainFrame extends JFrame implements PropertyChangeListener {
	private static final long serialVersionUID = -2524529952753111095L;

	private Container mainPanel = null;
	private JLabel lbInput = new JLabel("Please Select Folder that contain MS word documents.");
	private JLabel lbOutput = new JLabel("Please Select where to save.");
	private JButton btnInput = new JButton("Browse");
	private JButton btnOutput = new JButton("Extract");

	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel panel3 = new JPanel();

	private String inputDirString;
	private String SEPARATOR = ",";

	private Task task=null;

	private ProgressMonitor progressMonitor;

	private JTextArea taskOutput = new JTextArea(20,70);
	//	private JTextArea errOutput = new JTextArea();

	private class Task extends SwingWorker<Void, Void>{

		//		String warningStr = "";
		@Override
		protected Void doInBackground() throws Exception {
			String outputStr = FileChooser.saveFile();
			//			System.out.println(outputStr);

			setProgress(0);

			String[] filesString = MyFile.getAllFileName(inputDirString);
			ArrayList<String> allEmails = new ArrayList<String>();

			for(int i=0;i<filesString.length;i++){
				//				System.out.println(filesString[i]);
				String str = "";
				String extention = filesString[i].toLowerCase();
				try{
					if(extention.endsWith("doc")){
						str = MsWordDoc.getString(filesString[i]);
					}else if (extention.endsWith("docx")) {
						str = MsWordDocx.getString(filesString[i]);
					}else if (extention.endsWith("pdf")){
						str = SDWPdfReader.getString(filesString[i]);
					}else if (extention.endsWith("txt")){
						str = MyFile.loadAllAsString(filesString[i]);
					}
				}catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					taskOutput.append(e1.getMessage());
					continue;
				}

				ArrayList<String> emailList = GetEmail.getFromString(str);
				if(emailList == null || emailList.isEmpty()){
					System.out.println(filesString[i]);
					String msg = String.format("Warrning: This file may not contain emails in file: \t%s\n",  filesString[i]);
					//					warningStr+=msg;
					taskOutput.append(msg);
				}else if(emailList.size()>1){
					// add elements to al, including duplicates
					HashSet<String> hs = new HashSet<String>();
					hs.addAll(emailList);
					emailList.clear();
					emailList.addAll(hs);
					if(emailList.size()>1){
						String msg = String.format("\nWarrning: Multiple emails found in file: \t%s\n",  filesString[i]);
						msg +="They are: \t";
						for(String tmp : emailList){
							msg += tmp +", ";
						}
						msg +="\n\n";
						taskOutput.append(msg);
					}
				}
				allEmails.addAll(emailList);

				setProgress((int)((double)i/filesString.length *100));
			}

			String allStr="";
			for (String str : allEmails) {
				allStr +=str+SEPARATOR;
			}
			MyFile file = new MyFile(outputStr);
			file.overwrite(allStr.substring(0,allStr.length()-1));

			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnOutput.setEnabled(true);
			progressMonitor.setProgress(100);
			//			taskOutput.append(warningStr);
			taskOutput.append("Completed! \n");
		}

	}

	public MainFrame(){
		mainPanel =  this.getContentPane();
		this.setTitle("Extract emails for doc/docx files");
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel3.setLayout(new FlowLayout(FlowLayout.LEFT));

		mainPanel.add(panel1);
		mainPanel.add(panel2);
		mainPanel.add(panel3);


		JScrollPane scrollPane = new JScrollPane(taskOutput); 

		panel1.add(btnInput);
		panel1.add(lbInput);
		panel2.add(btnOutput);
		panel2.add(lbOutput);
		panel3.add(scrollPane);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);


		btnOutput.setActionCommand("start");

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();

		btnInput.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				inputDirString = FileChooser.selectFolder();
				lbInput.setText(inputDirString);
			}
		});

		btnOutput.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				progressMonitor = new ProgressMonitor(MainFrame.this,
						"Running a Long Task",
						"", 0, 100);
				progressMonitor.setProgress(0);
				task = new Task();
				task.addPropertyChangeListener(MainFrame.this);
				task.execute();

				btnOutput.setEnabled(false);

			}
		});

	}


	private static void createAndShowGUI() {
		//Create and set up the window.

		JFrame frame = new MainFrame();
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//		MainFrame mf = new MainFrame();
		//		mf.setVisible(true);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		//		System.out.println(evt.getPropertyName());
		if ("progress" == evt.getPropertyName() ) {
			//		if (true ) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			//            String message =
			//                String.format("Completed %d%%.\n", progress);
			//            progressMonitor.setNote(message);

			//            taskOutput.append(message);
			if (progressMonitor.isCanceled() || task.isDone()) {
				Toolkit.getDefaultToolkit().beep();
				if (progressMonitor.isCanceled()) {
					task.cancel(true);
					taskOutput.append("Task canceled.\n");
				} else {
					//                    taskOutput.append("Task completed.\n");
				}
				btnOutput.setEnabled(true);
			}
		}

	}

}
