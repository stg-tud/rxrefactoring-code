package shoudaw.msword;

import java.io.FileInputStream;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class MsWordDocx {

	/**
	 * @param args
	 */

	public static String getString(String filePath) throws Exception{

		try {
			XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath));
			XWPFWordExtractor wordxExtractor = new XWPFWordExtractor(doc);
			String text = wordxExtractor.getText();
//			System.out.println(text);
			return text;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String errStr = String.format("Error: %s , Please extract the email manually", filePath);
			throw new Exception(errStr);
		}

	}

}
