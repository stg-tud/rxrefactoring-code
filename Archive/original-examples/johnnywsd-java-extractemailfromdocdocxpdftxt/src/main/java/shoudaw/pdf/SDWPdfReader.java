package shoudaw.pdf;


import java.io.File;

import java.io.IOException;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class SDWPdfReader {
//	public static final String path = "/home/johnny/workspace/tmp/a.pdf";
//	public static final String path = "/home/johnny/workspace/tmp/b.pdf";
	
	public static String getString(String filePath) throws IOException{
		File input = new File(filePath); 
		String rtn = "";
		PDDocument pd;
		pd = PDDocument.load(input);
		PDFTextStripper stripper = new PDFTextStripper();	
		rtn = stripper.getText(pd);
		if (pd != null) {
			pd.close();
		}
		return rtn;
	}
	
	public static void main(String[] args) throws IOException{
//		String str = SDWPdfReader.getString(path);
//		System.out.print(str);
	}
}
