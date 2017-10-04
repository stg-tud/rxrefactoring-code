package de.tudarmstadt.rxrefactoring.ext.javafuture.domain;

import java.util.Arrays;
import java.util.List;

public class CollectionInfo {

	private static final String[] collectionBinaryNames = new String[] { "java.util.Collection" };

	public static final List<String> publicMethodsMap = Arrays.asList(

			);

	/**
	 *
	 * @return full name of the class. i.e: android.os.AsyncTask
	 */
	public static String[] getBinaryNames()
	{
		return collectionBinaryNames;
	}

}
