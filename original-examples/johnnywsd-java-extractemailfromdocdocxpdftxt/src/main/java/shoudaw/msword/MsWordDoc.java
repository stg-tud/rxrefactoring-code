package shoudaw.msword;

import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

public class MsWordDoc {

	public static String getString(String filePath) throws Exception{

		try {
//			System.out.println("Proceeding:"+filePath);
			HWPFDocument document=new HWPFDocument(new FileInputStream(filePath));
			WordExtractor extractor = new WordExtractor(document);
			String text = extractor.getText();
			return text;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String errStr = String.format("Error: %s , Please extract the email manually", filePath);
			throw new Exception(errStr);
		}

	}
	
	

}
