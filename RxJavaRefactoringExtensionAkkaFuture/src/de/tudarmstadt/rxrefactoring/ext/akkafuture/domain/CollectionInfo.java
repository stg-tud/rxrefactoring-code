package de.tudarmstadt.rxrefactoring.ext.akkafuture.domain;

import java.util.Arrays;
import java.util.List;

public class CollectionInfo {

	private static final List<String> collectionBinaryNames = Arrays.asList(
			"java.util.Collection"
			);

	public static final List<String> publicMethodsMap = Arrays.asList(

			);

	/**
	 *
	 * @return full name of the class. i.e: android.os.AsyncTask
	 */
	public static List<String> getBinaryNames()
	{
		return collectionBinaryNames;
	}

}
