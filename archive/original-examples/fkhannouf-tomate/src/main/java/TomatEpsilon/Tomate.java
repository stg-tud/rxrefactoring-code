package TomatEpsilon;

/*
 * @(#)Tomate.java	1.15	00/01/31
 *
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.File;
import java.util.Vector;
import javax.sound.sampled.*;
import javax.sound.midi.*;


/**
 *
 * @version @(#)Tomate.java	1.15 00/01/31
 * @author Frederic Khannouf  
 */
public class Tomate extends JPanel implements ChangeListener, Runnable {

    Vector demos = new Vector(4);
    JTabbedPane tabPane = new JTabbedPane();
    int width = 760, height = 540;
    int index;

    public Tomate(String audioDirectory) {

        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
       
        /*JMenu options = (JMenu) menuBar.add(new JMenu("Options"));
        JMenuItem item = (JMenuItem) options.add(new JMenuItem("Applet Info"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showInfoDialog(); }
        });*/
        add(menuBar, BorderLayout.NORTH);

        tabPane.addChangeListener(this);

        EmptyBorder eb = new EmptyBorder(5,5,5,5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        /*JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb,new EmptyBorder(0,0,90,0)));
        final Juke juke = new Juke(audioDirectory);
        p.add(juke);
        demos.add(juke);
        tabPane.addTab("Juke Box", p);
*/
        new Thread(this).start();

        add(tabPane, BorderLayout.CENTER);
    }


    public void stateChanged(ChangeEvent e) {
        close();
        System.gc();
        index = tabPane.getSelectedIndex();
        open();
    }


    public void close() {
        ((ControlContext) demos.get(index)).close();
    }


    public void open() {
        ((ControlContext) demos.get(index)).open();
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }


    public static void showInfoDialog() {
        final String msg = 
            "When running the Tomate as an applet these permissions\n" +
            "are necessary in order to load/save files and record audio :  \n\n"+
            "grant { \n" +
            "  permission java.io.FilePermission \"<<ALL FILES>>\", \"read, write\";\n" +
            "  permission javax.sound.sampled.AudioPermission \"record\"; \n" +
            "  permission java.util.PropertyPermission \"user.dir\", \"read\";\n"+
            "}; \n\n" +
            "The permissions need to be added to the .java.policy file.";
        new Thread(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, "Applet Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();
    }



    /**
     * Lazy load the tabbed pane with CapturePlayback, MidiSynth and Groove.
     */
    public void run() {
        EmptyBorder eb = new EmptyBorder(0,0,0,0);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);

        /*JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb,new EmptyBorder(0,0,90,0)));

        CapturePlayback capturePlayback = new CapturePlayback();
        demos.add(capturePlayback);
        p.add(capturePlayback);
        tabPane.addTab("Capture/Playback", p);

        MidiSynth midiSynth = new MidiSynth();
        demos.add(midiSynth);
        tabPane.addTab("Midi Synthesizer", midiSynth);

        p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb,new EmptyBorder(0,0,5,20)));
        Groove groove = new Groove();
        demos.add(groove);
        p.add(groove);
        tabPane.addTab("Groove Box", p);*/

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        GroupToXRNI groupToXRNI = new GroupToXRNI();
        demos.add(groupToXRNI);
        p.add(groupToXRNI);
        tabPane.addTab("GroupToXRNI", p);

        p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        ExternalMIDIToSamples externalMIDIToSamples = new ExternalMIDIToSamples();
        demos.add(groupToXRNI);
        p.add(groupToXRNI);
        tabPane.addTab("GroupToXRNI", p);

        p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        About aboutPanel = new About();
        demos.add(aboutPanel);
        p.add(aboutPanel);
        tabPane.addTab("About", p);

    }


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // com.sun.java.swing.plaf.mac.MacLookAndFeel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ex) {

                //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel());
        }
        
        try { 
            if (MidiSystem.getSequencer() == null) {
                System.err.println("MidiSystem Sequencer Unavailable, exiting!");
                System.exit(1);
            } else if (AudioSystem.getMixer(null) == null) {
                System.err.println("AudioSystem Unavailable, exiting!");
                System.exit(1);
            }
        } catch (Exception ex) { ex.printStackTrace(); System.exit(1); }

        String media = "./audio";
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file == null && !file.isDirectory()) {
                System.out.println("usage: java TomatE");
            } else {
                media = args[0];
            }
        }

        final Tomate tomate = new Tomate(media);
        JFrame f = new JFrame("Tomate");
        Package client=Package.getPackage("TomatEpsilon");
        f.setTitle("TomatE ("+client.getImplementationVersion()+")");
        //f.setTitle(tomate.getClass().getPackage().toString()/*.getImplementationVersion()*/);

/*System.out.println(" package="+client);
System.out.print(" Impl Version : "+client.getImplementationVersion());
System.out.println(" Spec Version : "+client.getSpecificationVersion());*/
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {System.exit(0);}
            @Override
            public void windowDeiconified(WindowEvent e) { tomate.open(); }
            @Override
            public void windowIconified(WindowEvent e) { tomate.close(); }
        });
        f.getContentPane().add("Center", tomate);
        f.pack();
        /*Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(d.width/2 - tomate.width/2, d.height/2 - tomate.height/2);
        f.setSize(new Dimension(tomate.width, tomate.height));
        f.setVisible(true);*/
        Misc.initComponentSize(f);
    }
}
