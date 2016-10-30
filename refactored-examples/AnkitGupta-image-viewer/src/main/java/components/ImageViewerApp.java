/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package components;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;


/**
 *
 * @author anks
 */


public class ImageViewerApp extends JFrame {
    
    private JLabel photographLabel = new JLabel();
    private JToolBar buttonBar = new JToolBar();
    
    private String imagedir = "/images/";
    
    private MissingIcon placeholderIcon = new MissingIcon();
    
    /**
     * List of all the descriptions of the image files with caption.
     */
    private String[] imageCaptions = { "Amazing Beauty 1", "Amazing Beauty 2",
    "Amazing Beauty 3", "Amazing Beauty 4"};
    
    /**
     * List of all the image files to load.
     */
    private String[] imageFileNames = { "1.jpg", "2.jpg",
    "3.jpg", "4.jpg"};

    /*
    KeyListener kl=new KeyAdapter()
     {
      @Override
      public void keyPressed(KeyEvent evt)
      {
       //If someone click Esc key, this program will exit
       if(evt.getKeyCode()==KeyEvent.VK_ESCAPE)
       {
        System.exit(0);
       }
      }
     };
     */

    /**
     * Main of the program. Loads the Swing elements on the "Event
     * Dispatch Thread".
     *
     * @param args
     */

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageViewerApp app = new ImageViewerApp();
                app.setVisible(true);                
            }
        });

    }
    
    /**
     * Default constructor
     */
    public ImageViewerApp() {

        //this.addKeyListener(kl);

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Image Viewer: Please Select an Image");
        
        // A label for displaying the pictures
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Two glue components to add thumbnail buttons to the toolbar inbetween thease glue compoents.
        buttonBar.add(Box.createGlue());
        buttonBar.add(Box.createGlue());
        
        add(buttonBar, BorderLayout.NORTH);
        add(photographLabel, BorderLayout.CENTER);        

        setSize(900, 700);
        //this.setResizable(false);

        // switching to fullscreen mode
        //GraphicsEnvironment.getLocalGraphicsEnvironment().
        //getDefaultScreenDevice().setFullScreenWindow(this);

        // this centers the frame on the screen
        setLocationRelativeTo(null);
        
        // start the image loading SwingWorker in a background thread

        // RxRefactoring: Subscriber needed to update the UI
        Subscriber<ThumbnailAction> publishSubscriber = getThumbnailActionSubscriber();

        Observable.fromCallable(() -> loadImagesSync(publishSubscriber))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.immediate())
                .subscribe();

    }

    private Subscriber<ThumbnailAction> getThumbnailActionSubscriber()
    {
        return new Subscriber<ThumbnailAction>()
            {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable throwable)
                {

                }

                @Override
                public void onNext(ThumbnailAction thumbnailAction)
                {
                    JButton thumbButton = new JButton(thumbnailAction);
                    buttonBar.add(thumbButton, buttonBar.getComponentCount() - 1);
                }
            };
    }

    // RxRefactoring: publish method is replaced by a subscriber and "onNext"
    public Void loadImagesSync(Subscriber publishSubscriber)
    {
        for (int i = 0; i < imageCaptions.length; i++) {
            ImageIcon icon;
            icon = createImageIcon(imagedir + imageFileNames[i], imageCaptions[i]);

            ThumbnailAction thumbAction;
            if(icon != null){

                ImageIcon thumbnailIcon = new ImageIcon(getScaledImage(icon.getImage(), 64, 64));

                thumbAction = new ThumbnailAction(icon, thumbnailIcon, imageCaptions[i]);

            }else{
                // the image failed to load for some reason
                // so load a placeholder instead
                thumbAction = new ThumbnailAction(placeholderIcon, placeholderIcon, imageCaptions[i]);
            }
            publishSubscriber.onNext(thumbAction);
        }
        return null;
    }
    //Create a KeyListener that can listen when someone press Esc key on keyboard
    //You can change for what key that you want, by change value at:
    //VK_ESCAPE

    
    /**
     * SwingWorker class that loads the images a background thread and calls publish
     * when a new one is ready to be displayed.
     */
    
    /**
     * Creates an ImageIcon if the path is valid.
     * @param String - resource path
     * @param String - description of the file
     */
    protected ImageIcon createImageIcon(String path,
            String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * @param srcImg - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }
    
    /**
     * Action class that shows the image specified in it's constructor.
     */
    private class ThumbnailAction extends AbstractAction{
        
        /**
         *The icon if the full image we want to display.
         */
        private Icon displayPhoto;
        
        /**
         * @param Icon - The full size photo to show in the button.
         * @param Icon - The thumbnail to show in the button.
         * @param String - The descriptioon of the icon.
         */
        public ThumbnailAction(Icon photo, Icon thumb, String desc){
            displayPhoto = photo;
            
            // The short description becomes the tooltip of a button.
            putValue(SHORT_DESCRIPTION, desc);
            
            // The LARGE_ICON_KEY is the key for setting the
            // icon when an Action is applied to a button.
            putValue(LARGE_ICON_KEY, thumb);
        }
        
        /**
         * Shows the full image in the main area and sets the application title.
         */
        public void actionPerformed(ActionEvent e) {
            photographLabel.setIcon(displayPhoto);
            setTitle("Image Viewer: " + getValue(SHORT_DESCRIPTION).toString());
        }
    }
}
