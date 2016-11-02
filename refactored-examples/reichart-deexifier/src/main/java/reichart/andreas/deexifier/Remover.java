/*******************************************************************************
 * Copyright 2012 Andreas Reichart. Distributed under the terms of the GNU General Public License.
 * 
 * This file is part of DeExifier.
 * 
 * DeExifier is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DeExifier is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeExifier. If not,
 * see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 * 
 */
package reichart.andreas.deexifier;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.SanselanConstants;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.common.byteSources.ByteSource;
import org.apache.sanselan.common.byteSources.ByteSourceArray;
import org.apache.sanselan.common.byteSources.ByteSourceFile;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.jpeg.iptc.IPTCConstants;
import org.apache.sanselan.formats.jpeg.iptc.IPTCRecord;
import org.apache.sanselan.formats.jpeg.iptc.JpegIptcRewriter;
import org.apache.sanselan.formats.jpeg.iptc.PhotoshopApp13Data;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import com.sun.org.apache.bcel.internal.classfile.PMGClass;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Removes metadata from jpg images by extending Swingworker.<br>
 * <br>
 * {@link #setParams(ArrayList, File, int, boolean, String, JList, JProgressBar, JLabel)}
 * 
 * @author Andreas Reichart
 */
class Remover {
    private ArrayList<File> fileList;
    private File path;
    private final String[] JPG_SUFFIXES = { ".jpg", ".jpeg", ".JPG", ".JPEG" };
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private int progressCounter = 0;
    private int compressionQuality;
    private boolean recompress;
    private String additionalSuffix;
    private int width;
    private int scalingFactor;
    private boolean removeExif;
    private boolean removeIptc;

    public Remover() {
	removeExif = false;
	removeIptc = false;
	scalingFactor = 100;
	fileList = new ArrayList<File>();
    }

    void setParams(ArrayList<File> fileList, File path, int compressionQuality, boolean recompress, String addSuffix,
	    JList<String> list, JProgressBar progressBar, JLabel statusLabel) {
	this.fileList = fileList;
	this.path = path;
	this.listModel = (DefaultListModel<String>) list.getModel();
	this.progressBar = progressBar;
	this.recompress = recompress;
	this.compressionQuality = compressionQuality;
	this.additionalSuffix = addSuffix;
	this.statusLabel = statusLabel;
	// this.list = list;
	progressBar.setMinimum(0);
	progressBar.setMaximum(fileList.size());

    }

    void setWidth(int width) {
	this.width = width;
	scalingFactor = 0;
	recompress = true;
    }

    void setScale(int scalingFactor) {
	this.scalingFactor = scalingFactor;
	width = 0;
	recompress = true;
    }

    void setRemoveOptions(boolean removeExif, boolean removeIptc) {
	this.removeExif = removeExif;
	this.removeIptc = removeIptc;
    }

    // RxRefactoring: subscriber added for updating
    private Void doInBackground(Subscriber<String> subscriber) throws ImageReadException, IOException, ImageWriteException {
	// int counter = 0;

	// iterate over the fileList
	for (File f : fileList) {

	    // rename correctly
	    String destFileName = "";
	    String fName = f.getName();
	    for (int i = 4; i <= 5; i++) {
		for (String s : JPG_SUFFIXES) {
		    if (fName.substring(fName.length() - i).equals(s)) {
			destFileName = fName.substring(0, fName.length() - i) + additionalSuffix + ".jpg";
		    }
		}
	    }

	    // FileOutputStream fileOutStream = null;
	    BufferedOutputStream bOutputStream = new BufferedOutputStream(new FileOutputStream(path + File.separator
		    + destFileName));
	    ExifRewriter eRewriter = new ExifRewriter();
	    JpegIptcRewriter iRewriter = new JpegIptcRewriter();
	    Resizer resizer = new Resizer();

	    // do we need to recompress the whole thing?
	    if (recompress) {
		// recompression is always on when scaling: prepare some default values
		if (width == 0 && scalingFactor == 0)
		    scalingFactor = 100;

		// resize and put the result into a byte Array
		byte[] imageByte = resizer.resize(f, width, scalingFactor, (float) compressionQuality / 100);

		// OK this works
		if (removeExif && !removeIptc) {
		    // eRewriter.removeExifMetadata(imageByte, bOutputStream); // cheap and dirty

		    HashMap<String, Boolean> params = new HashMap<>();
		    params.put(SanselanConstants.PARAM_KEY_READ_THUMBNAILS, false);
		    JpegPhotoshopMetadata pMetaData = new JpegImageParser().getPhotoshopMetadata(new ByteSourceFile(f),
			    params);
		    List<IPTCRecord> iptcRecords;
		    List<?> nonIptcBlocks = null;
		    if (pMetaData != null) {// pick existing IPTC Data
			nonIptcBlocks = pMetaData.photoshopApp13Data.getNonIptcBlocks();
			iptcRecords = pMetaData.photoshopApp13Data.getRecords();
		    } else {
			iptcRecords = new ArrayList<IPTCRecord>();
			nonIptcBlocks = new ArrayList<>();
		    }
		    List<IPTCRecord> newRecords = new ArrayList<>();

		    for (IPTCRecord record : iptcRecords) {
			if (record.iptcType.type == IPTCConstants.IPTC_TYPE_ORIGINATING_PROGRAM.type) {
			    newRecords.add(new IPTCRecord(IPTCConstants.IPTC_TYPE_ORIGINATING_PROGRAM,
				    "Dexifier by Andreas Reichart <andreas.reichart@gmail.com>"));
			} else {
			    newRecords.add(record);
			}
		    }

		    PhotoshopApp13Data pApp13Data = new PhotoshopApp13Data(newRecords, nonIptcBlocks);
		    iRewriter.writeIPTC(imageByte, bOutputStream, pApp13Data);

		}

		if (!removeExif && removeIptc) {
		    HashMap<String, Boolean> params = new HashMap<>();
		    params.put(SanselanConstants.PARAM_KEY_READ_THUMBNAILS, false);
		    JpegPhotoshopMetadata pMetaData = new JpegImageParser().getPhotoshopMetadata(new ByteSourceFile(f),
			    params);
		    assert (pMetaData != null);
		    // List<?> emptyRecords = pMetaData.photoshopApp13Data.getRawBlocks();

		    List<IPTCRecord> emptyRecords = new ArrayList<IPTCRecord>();
		    List<IPTCRecord> newRecords = new ArrayList<IPTCRecord>();
		    // List<IPTCRecord> emptyRecords = new ArrayList<IPTCRecord>();
		    newRecords.add(new IPTCRecord(IPTCConstants.IPTC_TYPE_SPECIAL_INSTRUCTIONS,
			    "Converted by DeExifier"));
		    // generate an empty App13 DataBlock
		    PhotoshopApp13Data newMetaData = new PhotoshopApp13Data(newRecords, emptyRecords);
		    // iRewriter.removeIPTC(resizedImageByte, bOutputStream);
		    // eRewriter.updateExifMetadataLossless(imageByte, bOutputStream,
		    // jmd.getExif().getOutputSet());
		    iRewriter.writeIPTC(imageByte, bOutputStream, newMetaData);
		    // TODO: should be an eRewriter to get back the Exif part!

		    IImageMetadata metaData = Sanselan.getMetadata(f);
		    JpegImageMetadata jMetaData = (JpegImageMetadata) metaData;
		    assert (jMetaData != null);
		    TiffImageMetadata exif = jMetaData.getExif();
		    TiffOutputSet outputSet = exif.getOutputSet();
		    TiffOutputField field = outputSet.findField(TiffConstants.EXIF_TAG_PROCESSING_SOFTWARE);
		    if (null != field) {
			outputSet.removeField(TiffConstants.EXIF_TAG_PROCESSING_SOFTWARE);
			String fieldString = "Edited by DeExifier. andreas.reichart@gmail.com";
			TiffOutputField newField = new TiffOutputField(ExifTagConstants.EXIF_TAG_PROCESSING_SOFTWARE,
				TiffFieldTypeConstants.FIELD_TYPE_ASCII, fieldString.length(), fieldString.getBytes());
			TiffOutputDirectory outDirectory = outputSet.getOrCreateExifDirectory();
			outDirectory.add(newField);
		    }

		    eRewriter.updateExifMetadataLossless(imageByte, bOutputStream, outputSet);

		}

		// if (removeExif & removeIptc) {
		// TODO: removeExif & removeIptc
		// }

		// OK, works: recompressed image is already empty.
		if (!removeExif && !removeIptc) {

		    IImageMetadata metadata = Sanselan.getMetadata(f);
		    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		    if (jpegMetadata != null) {
			TiffImageMetadata exif = jpegMetadata.getExif();
			if (exif != null) {
			    TiffOutputSet outputSet = exif.getOutputSet();
			    TiffOutputSet emptySet = new TiffOutputSet();
			    new ExifRewriter().removeExifMetadata(imageByte, bOutputStream);
			}
		    }
		}
	    }

	    // Okiedokie: de-Exify without recompression or resizing
	    else {

		if (removeExif && !removeIptc) {
		    // only removing Exif data
		    // if no recompression should be done, we use the standard Sanselan ExifRewriter
		    eRewriter.removeExifMetadata(f, bOutputStream);

		} else if (!removeExif && removeIptc) {
		    // only removing the IPTC Data
		    // iRewriter.removeIPTC(f, bOutputStream); // simple version
		    Map<String, Boolean> params = new HashMap<>();
		    params.put(SanselanConstants.PARAM_KEY_READ_THUMBNAILS, false);
		    JpegPhotoshopMetadata jpMetadata = new JpegImageParser().getPhotoshopMetadata(
			    new ByteSourceFile(f), params);
		    List<?> nonIPTCBlocks = jpMetadata.photoshopApp13Data.getNonIptcBlocks();
		    List<IPTCRecord> newRecords = new ArrayList<>();
		    PhotoshopApp13Data pApp13Data = new PhotoshopApp13Data(newRecords, nonIPTCBlocks);
		    iRewriter.writeIPTC(f, bOutputStream, pApp13Data);

		} else if (removeExif && removeIptc) {
		    // removing Exif and IPTC metadata

		} else {
		    // removing nothing ... for what reason ?
		}

	    }

	    eRewriter = null;

	    bOutputStream.flush();
	    bOutputStream.close();

	    // DEBUG:
	    // BufferedOutputStream bOutputStreamTemp = new BufferedOutputStream(new
	    // FileOutputStream(path
	    // + File.separator + "00" + destFileName));
	    //
	    // TiffOutputSet oSet = getSanselanOutputSet(new File(path + File.separator +
	    // destFileName));
	    // new ExifRewriter().updateExifMetadataLossy(new File(path + File.separator +
	    // destFileName),
	    // bOutputStreamTemp, oSet);
	    //
	    // bOutputStreamTemp.flush();
	    // bOutputStreamTemp.close();
	    // END debug;

		subscriber.onNext(f.toString()); // RxRefactoring: subscribe.onNext instead of publish

	    // counter++;
	    // setProgress(counter);
	}
	subscriber.onCompleted();
	return null;
    }

    private void done() {
	fileList = null;
	listModel.removeAllElements();

	// statusLabel.setText("Done.");
    }

    // RxRefactoring: process is no longer needed
//    private void process(List<String> chunks) {
//	for (String f : chunks) {
//	    listModel.removeElement(f);
//	    fileList.remove(new File(f));
//	    progressBar.setValue(progressCounter++);
//	}
//    }

	// RxRefactoring: creates the observable that is in charge of executin the async task
    public Observable<Void> createRxObservable()
	{
		Subscriber<String> subscriber = createUpdateSubscriber();
		return Observable.fromCallable(() -> doInBackground(subscriber))
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.doOnCompleted(() -> done());
	}

	// RxRefactoring: defines how the UI should be updated
	private Subscriber<String> createUpdateSubscriber()
	{
		return new Subscriber<String>()
		{
			List<String> chunks = new ArrayList<>();
			@Override
			public void onCompleted()
			{
				for (String s: chunks)
				{
					listModel.removeElement(s);
					fileList.remove(new File(s));
				}
			}

			@Override
			public void onError(Throwable throwable)
			{

			}

			@Override
			public void onNext(String s)
			{
				// we have to collect the information first to avoid ConcurrentModificationException in the for loop
				chunks.add(s);
				progressBar.setValue(progressCounter++);
			}
		};
	}

    /*
     * private static void copyExifData(File sourceFile, File destFile, List<TagInfo>
     * excludedFields) { String tempFileName = destFile.getAbsolutePath() + ".tmp"; File tempFile =
     * null; OutputStream tempStream = null; try { tempFile = new File(tempFileName); TiffOutputSet
     * sourceSet = getSanselanOutputSet(sourceFile); TiffOutputSet destSet =
     * getSanselanOutputSet(destFile); destSet.getOrCreateExifDirectory(); // Go through the source
     * directories List<?> sourceDirectories = sourceSet.getDirectories(); for (int i = 0; i <
     * sourceDirectories.size(); i++) { TiffOutputDirectory sourceDirectory = (TiffOutputDirectory)
     * sourceDirectories.get(i); TiffOutputDirectory destinationDirectory =
     * getOrCreateExifDirectory(destSet, sourceDirectory); if (destinationDirectory == null)
     * continue; // failed to create // Loop the fields List<?> sourceFields =
     * sourceDirectory.getFields(); for (int j = 0; j < sourceFields.size(); j++) { // Get the
     * source field TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j); // Check
     * exclusion list if (excludedFields.contains(sourceField.tagInfo)) {
     * destinationDirectory.removeField(sourceField.tagInfo); continue; } // Remove any existing
     * field destinationDirectory.removeField(sourceField.tagInfo); // Add field
     * destinationDirectory.add(sourceField); } } // Save data to destination tempStream = new
     * BufferedOutputStream(new FileOutputStream(tempFile)); new
     * ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet); tempStream.close();
     * // Replace file if (destFile.delete()) { tempFile.renameTo(destFile); } } catch
     * (ImageReadException exception) { exception.printStackTrace(); } catch (ImageWriteException
     * exception) { exception.printStackTrace(); } catch (IOException exception) {
     * exception.printStackTrace(); } finally { if (tempStream != null) { try { tempStream.close();
     * } catch (IOException e) { } } if (tempFile != null) { if (tempFile.exists())
     * tempFile.delete(); } } } private static TiffOutputSet getSanselanOutputSet(File
     * jpegImageFile) throws IOException, ImageReadException, ImageWriteException { TiffOutputSet
     * outputSet = null; // note that metadata might be null if no metadata is found. IImageMetadata
     * metadata = Sanselan.getMetadata(jpegImageFile); JpegImageMetadata jpegMetadata =
     * (JpegImageMetadata) metadata; if (jpegMetadata != null) { // note that exif might be null if
     * no Exif metadata is found. TiffImageMetadata exif = jpegMetadata.getExif(); if (exif != null)
     * { outputSet = exif.getOutputSet(); } } // if file does not contain any exif metadata, we
     * create an empty // set of exif metadata. Otherwise, we keep all of the other // existing
     * tags. if (outputSet == null) outputSet = new TiffOutputSet(); // Return return outputSet; }
     * private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet,
     * TiffOutputDirectory outputDirectory) { TiffOutputDirectory result =
     * outputSet.findDirectory(outputDirectory.type); if (result != null) return result; result =
     * new TiffOutputDirectory(outputDirectory.type); try { outputSet.addDirectory(result); } catch
     * (ImageWriteException e) { return null; } return result; }
     */
}
