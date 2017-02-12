package ru.ifmo.juneiform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

/**
 *
 * @author Ivan Stepuk
 */
public abstract class DocumentLoader extends SwingWorker<List<Document>, Document> {

    private static final Logger log = Logger.getLogger(DocumentLoader.class);
    private File[] files;
    private Settings settings;
    
    public DocumentLoader() {
        
    }
    
    public DocumentLoader(Settings settings) {
        this();
        this.settings = settings;
    }
    
    public void load(File... files) {
        this.files = files;
        execute();
    }

    @Override
    protected List<Document> doInBackground() throws Exception {
        List<Document> result = new ArrayList<Document>();
        for (File file : files) {
            try {
                publish(new Document(
                        file.getName(),
                        file.getAbsolutePath(),
                        Language.values()[settings.getRecognitionLanguageId()],
                        ImageIO.read(file)));
            } catch (IOException ex) {
                log.warn("Could not read image from file " + file);
            }
        }
        return result;
    }

    @Override
    protected void process(List<Document> chunks)
    {
        fetchResult(chunks);
    }

    public abstract void fetchResult(List<Document> result);
}
