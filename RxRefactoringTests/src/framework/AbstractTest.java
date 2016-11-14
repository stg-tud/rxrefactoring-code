package framework;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Description: Abstract test class for common functions<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public class AbstractTest
{
    /**
     * Get the source code as a string given a path
     *
     * @param path
     *            the path of the source including the file name with the
     *            extension. This path is searched under resources<br>
     *            Example: for /resources/example.directory/Example.java use the
     *            following arguments: <br>
     *            "example.directory", "Example.java"
     * @return The source code as a String
     * @throws IOException
     */
    protected String getSourceCode( String... path ) throws IOException
    {
        Path p = Paths.get( "resources", path );
        FileInputStream in = new FileInputStream( p.toString() );
        StringBuilder sb = new StringBuilder();
        int ch;
        while ( ( ch = in.read() ) != -1 )
        {
            sb.append( (char) ch );
        }
        return sb.toString();
    }

    /**
     * This method can be used to compare to source codes without considering line breaks
     * @param expectedSourceCode expected source code
     * @param actualSourceCode actual source code
     */
    protected void assertEqualSourceCodes( String expectedSourceCode, String actualSourceCode )
    {
        String inputSourceCodeStandardFormat = getStandardFormat( actualSourceCode );
        String expectedSourceCodeStandardFormat = getStandardFormat( expectedSourceCode );
        assertEquals( expectedSourceCodeStandardFormat, inputSourceCodeStandardFormat );
    }

    /**
     * Compares all compilation units in result with the target file and returns its source code
     * @param targetFile target file
     * @param results map containing results
     * @return source code of the target file or emtpy if the file is not found
     */
    protected String getSourceCodeByFileName(String targetFile, Map<ICompilationUnit, String> results)
    {
        String actualSourceCode = "";
        for (ICompilationUnit cu : results.keySet())
        {
            if (targetFile.equals(cu.getElementName()))
            {
                return results.get(cu);
            }
        }
        return actualSourceCode;
    }

    // ### Private Methods ###

    private String getStandardFormat( String source )
    {
        ASTParser javaParser = ASTParser.newParser( AST.JLS8 );
        javaParser.setSource( source.toCharArray() );
        CompilationUnit compilationUnit = (CompilationUnit) javaParser.createAST( null );
        return compilationUnit.toString();
    }
}
