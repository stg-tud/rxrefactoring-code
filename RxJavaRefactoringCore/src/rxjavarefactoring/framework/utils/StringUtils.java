package rxjavarefactoring.framework.utils;

import org.eclipse.jdt.core.dom.Block;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public final class StringUtils
{
    private StringUtils()
    {

    }

    public static String removeBlockBraces( Block block )
    {
        String blockCode = block.toString();
        String cleanedBlock = replaceLast( blockCode.replaceFirst( "\\{", "" ), "\\}", "" );
        return cleanedBlock;
    }

    private static String replaceLast( String text, String regex, String replacement )
    {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
