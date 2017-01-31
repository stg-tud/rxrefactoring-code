package shoudaw.gui.file;

import java.io.File;


import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser {

	/**
	 * @param args
	 */
	public static String chooseFile(){
		JFileChooser fc = new JFileChooser();

		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if(f.getName().toLowerCase().endsWith("txt"))
					return true;
				return false;
			}

			//The description of this filter
			@Override
			public String getDescription() {
				return "Pain Text (.txt)";
			}

		});
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			//This is where a real application would save the file.

			if(!file.getAbsolutePath().toLowerCase().endsWith(".txt"))
			{
				return file.getAbsolutePath()+".txt";
			}else
				return file.getAbsolutePath();
		}else{
			return "No File";
		}

	}
	
	
	public static String saveFile(){
		JFileChooser fc = new JFileChooser();

		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if(f.getName().toLowerCase().endsWith("txt"))
					return true;
				return false;
			}

			//The description of this filter
			@Override
			public String getDescription() {
				return "Pain Text (.txt)";
			}

		});
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			//This is where a real application would save the file.

			if(!file.getAbsolutePath().toLowerCase().endsWith(".txt"))
			{
				return file.getAbsolutePath()+".txt";
			}else
				return file.getAbsolutePath();
		}else{
			return "No File";
		}

	}

	public static String selectFolder(){
		JFileChooser fc = new JFileChooser();

		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		fc.addChoosableFileFilter(new FileFilter(){
//
//			@Override
//			public boolean accept(File f) {
//				if (f.isDirectory()) {
//					return true;
//				}
//				if(f.getName().toLowerCase().endsWith("doc"))
//					return true;
//				return false;
//			}
//
//			//The description of this filter
//			@Override
//			public String getDescription() {
//				return "MS Word File (.doc)";
//			}
//
//		});
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			//This is where a real application would save the file.

//			if(!file.getAbsolutePath().toLowerCase().endsWith(".doc"))
//			{
//				return file.getAbsolutePath()+".doc";
//			}else
				return file.getAbsolutePath();
		}else{
			return null;
		}

	}

}

