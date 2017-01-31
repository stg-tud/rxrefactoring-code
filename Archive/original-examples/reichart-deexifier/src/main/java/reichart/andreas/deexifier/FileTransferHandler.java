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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * @author reichart
 * 
 */
public class FileTransferHandler extends TransferHandler {

    DataFlavor fileFlavorJpg;
    DataFlavor fileFlavorJpgFile;
    List<File> fileList;
    List<String> fileStringList;

    /**
     * 
     */
    private static final long serialVersionUID = 7946362886804989707L;

    /**
     * 
     */
    public FileTransferHandler() {
	super();
	// fileFlavorJpg = new DataFlavor("image/jpeg", "JPG File"); // TODO:Windows version
	try {
	    // fileFlavorJpg = new DataFlavor("text/uri-list;class=java.lang.String");
	    fileFlavorJpg = new DataFlavor("image/x-java-image;class=java.awt.Image");
	    fileFlavorJpgFile = new DataFlavor("image/jpeg", "JPG File"); // DELETEME???
	    // java.awt.datatransfer.DataFlavor[mimetype=image/x-java-image;representationclass=java.awt.Image]=[PNG,
	    // JFIF]
	} catch (ClassNotFoundException e) {
	    System.out.println("Class not found exception\n" + e.getLocalizedMessage());
	    e.printStackTrace();
	}
	fileList = new ArrayList<File>();
    }

    /**
     * @param property
     */
    public FileTransferHandler(String property) {
	super(property);
    }

    public boolean canImport(TransferHandler.TransferSupport support) {
	if (support.isDataFlavorSupported(fileFlavorJpg) | support.isDataFlavorSupported(fileFlavorJpgFile)) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
	if (!canImport(support))
	    return false;

	Transferable transferable = support.getTransferable();

	// try {
	// fileList = (List<File>) transferable.getTransferData(fileFlavorJpg);
	// } catch (UnsupportedFlavorException | IOException e) {
	// e.printStackTrace(); //TODO: can we delete that? Maybe some statusoutput?
	// return false;
	// }

	try {
	    Object transferredObject = transferable.getTransferData(fileFlavorJpg);

	    @SuppressWarnings("unchecked")
	    List<String> list = (List<String>) transferredObject;
	    // List<String> list = (List<String>) transferable.getTransferData(fileFlavorJpg);
	    // transferable.
	    fileStringList = list;
	    System.out.println(list.toString());
	} catch (UnsupportedFlavorException | IOException e) {
	    e.printStackTrace();
	}

	// for (File file : fileList) {
	//
	// }

	for (String fileString : fileStringList) {
	    System.out.println(fileString);
	}
	return true;
    }

}
