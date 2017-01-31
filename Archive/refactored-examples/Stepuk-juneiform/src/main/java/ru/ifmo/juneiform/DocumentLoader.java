package ru.ifmo.juneiform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 *
 * @author Ivan Stepuk
 */
public abstract class DocumentLoader {//} extends SwingWorker<List<Document>, Void> {

    private static final Logger log = Logger.getLogger(DocumentLoader.class);
    private File[] files;
    private Settings settings;
    
    public DocumentLoader() {
        
    }
    
    public DocumentLoader(Settings settings) {
        this();
        this.settings = settings;
    }

    // RxRefactoring: execute changed to createRxObservable().subscribe().
    public void load(File... files) {
        this.files = files;
        createRxObservable().subscribe();
    }

    // RxRefactoring: the execute() call was done within this class, therefore this method can be private
    private Observable<List<Document>> createRxObservable()
    {
        final List<Document> documents = new ArrayList<>();
        return Observable.fromCallable(() -> doInBackground())
                .doOnNext(r -> documents.addAll(r))
                .doOnCompleted(() -> done(documents))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.immediate());

    }

    protected List<Document> doInBackground() throws Exception {
        List<Document> result = new ArrayList<Document>();
        for (File file : files) {
            try {
                result.add(new Document(
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

    protected void done(List<Document> document) {
        try {
            fetchResult(document);
        } catch (Exception ex) {
            log.warn("SwingWorker error");
        }
    }

    public abstract void fetchResult(List<Document> result);
}
