package framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: Data transfer object to transport relevant information about
 * files located in the resources folders. This files can be used to write input
 * and expected classes<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public class TestFilesDto
{
	private final String packageName;
	private final String directoryName;
	private final List<String> classNames;

    /**
     * Constructor for test files.
     * @param directoryName directory name in folder "resources"
     * @param packageName a name for the package
     * @param fileName the file name (without the extension ".java")
     */
	public TestFilesDto( String directoryName, String packageName, String fileName )
	{
		this.directoryName = directoryName;
		this.packageName = packageName;
		this.classNames = new ArrayList<>();
		classNames.add( fileName );
	}

    /**
     * Method to add more files to this object
     * @param fileNames name of the files without the ".java" extension
     */
	public void addClasses( String... fileNames )
	{
		this.classNames.addAll( Arrays.asList( fileNames ) );
	}

	public String getPackageName()
	{
		return packageName;
	}

	public String getDirectoryName()
	{
		return directoryName;
	}

	public List<String> getClassNames()
	{
		return classNames;
	}
}
