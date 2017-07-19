package de.tudarmstadt.rxrefactoring.core.utils;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Description: Class to manipulate strings <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public final class StringUtils
{
	private StringUtils()
	{
		// this class should not be instantiated
	}

	/**
	 * Removes the first and the last found braces "{", "}" in a
	 * {@link Block}
	 * 
	 * @param block
	 *            block
	 * @return the piece of code contained in the block
	 */
	public static String removeBlockBraces( Block block )
	{
		String blockCode = block.toString();
		String cleanedBlock = replaceLast( blockCode.replaceFirst( "\\{", "" ), "\\}", "" );
		return cleanedBlock;
	}

	/**
	 * Retrieves the binary name of a compilation unit
	 * 
	 * @param cu
	 *            target compilation unit
	 * @return binary name of the compilation unit
	 */
	public static String getCompilationUnitFullName( CompilationUnit cu )
	{
		return cu.getPackage().toString()
				.replaceAll( "package ", "" )
				.replaceAll( ";", "." + cu.getJavaElement().getElementName() )
				.replaceAll( "\n", "" )
				.replaceAll( "\\.java", "" );
	}

	// ### Private Methods ###

	private static String replaceLast( String text, String regex, String replacement )
	{
		return text.replaceFirst( "(?s)(.*)" + regex, "$1" + replacement );
	}
}
