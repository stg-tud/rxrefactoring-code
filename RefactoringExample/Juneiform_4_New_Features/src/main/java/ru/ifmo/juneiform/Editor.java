/*
 * Copyright 2011-2012 The Juneiform Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ifmo.juneiform;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWPackage;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;

import java.awt.Cursor;
import java.io.File;
import java.util.Random;

import org.apache.log4j.Logger;
import ru.ifmo.juneiform.ocr.Cuneiform;
import ru.ifmo.juneiform.ocr.CuneiformException;
import ru.ifmo.juneiform.ocr.OCRException;
import ru.ifmo.juneiform.scanner.ScanException;
import ru.ifmo.juneiform.scanner.XSane;
import ru.ifmo.juneiform.ui.InputPanel;
import ru.ifmo.juneiform.ui.MouseCoordinatesListener;
import ru.ifmo.juneiform.ui.View;
import ru.ifmo.juneiform.ui.dialogs.Utils;
import rx.Emitter;

/**
 *
 * @author Ivan Stepuk
 * @author Oleg Kuznetsov
 * @author felleet
 */
public class Editor implements ViewInteractor {

    public static final double ZOOM_DELTA = 0.2;
    private static final Logger log = Logger.getLogger(Controller.class);
    private Cuneiform cuneiform;
    private MouseCoordinatesListener mouseCoordinatesListener = MouseCoordinatesListener.getInstance();
    private View view;
    private Document document;
    private InputPanel inputPanel = InputPanel.getInstance();
    private XSane xSane;
    private File tempFile;

    public Editor(Cuneiform cuneiform) {
        this.cuneiform = cuneiform;
    }

    @Override
    public void injectView(View view) {
        this.view = view;
    }

    public void openDocument(Document document) {
        if (this.document != null) {
            this.document.unregisterObserver();
        }

        if (!document.equals(this.document)) {
            this.document = document;
            document.registerObserver(mouseCoordinatesListener);
            view.setLanguage(document.getLanguage());
            view.fillOutput(document.getText());
        }
        inputPanel.renderDocument(document);
    }

    public void closeDocument() {
        this.document.unregisterObserver();
        this.document = null;
        inputPanel.clearRenderArea();
        view.fillOutput("");
    }

    public Document getOpenedDocument() {
        return document;
    }

    public void performOCR() throws CuneiformException {
        cuneiform.setLanguage(document.getLanguage());
        cuneiform.setOutputFile(new File(document.getPath().concat(".ocr")));

        view.setStatus(L10n.forString("status.ocr"));
        view.changeCursor(new Cursor(Cursor.WAIT_CURSOR));
        rx.Observable<SWPackage<String, Void>> rxObservable = rx.Observable.fromEmitter(new SWEmitter<String, Void>() {
			@Override
			protected String doInBackground() throws Exception {
				String result = "";
				try {
					result = cuneiform.performOCR(document.getPath(), true);
				} catch (OCRException ex) {
					view.showErrorDialog(L10n.forString("msg.error"), ex.getMessage());
				} finally {
					view.clearStatus(L10n.forString("status.ocr"));
					view.changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return result;
				}
			}
        }, Emitter.BackpressureMode.BUFFER )
                .doOnSubscribe( () -> Utils.showLoadingSpinner() )
                .doOnNext( swPackage -> copyToClipBoard( swPackage.getResult() ) )
                .doOnCompleted( () -> Utils.closeLoadingSpinner() );

		SWSubscriber<String, Void> rxObserver = new SWSubscriber<String, Void>(rxObservable) {
			@Override
			protected void done() {
				try {
					String result = get();
					document.setText(result);
					view.fillOutput(result);
				} catch (Exception ex) {
					log.warn("SwingWorker error");
				}
			}
		};

		rxObserver.executeObservable();
    }

    public File scanImage() {

        Random r = new Random();
        tempFile = new File(System.getProperty("java.io.tmpdir") + "/temp-" + r.nextInt(10000) + ".jpg");
        tempFile.deleteOnExit();

        xSane = new XSane(tempFile);
        try {
            xSane.performScan();
        } catch (ScanException ex) {
            view.showErrorDialog(L10n.forString("msg.error"), ex.getMessage());
        }
        return tempFile;
    }

    void zoomIn() {
        try {
            document.modifyZoomFactor(+ZOOM_DELTA);
            openDocument(document);
        } catch (DocumentException ex) {
            // Ignore
        }
    }

    void zoomOut() {
        try {
            document.modifyZoomFactor(-ZOOM_DELTA);
            openDocument(document);
        } catch (DocumentException ex) {
            // Ignore
        }
    }

    void originalSize() {
        try {
            document.modifyZoomFactor(1 - document.getZoomFactor());
            openDocument(document);
        } catch (DocumentException ex) {
            // Ignore
        }
    }

    void rotateLeft() {
        document.rotateImageLeft();
        openDocument(document);
    }

    void rotateRight() {
        document.rotateImageRight();
        openDocument(document);
    }

    void copyToClipBoard( String text )
    {
        StringSelection stringSelection = new StringSelection( text );
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents( stringSelection, null );
    }
}