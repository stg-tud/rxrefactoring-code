package shoudaw.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetEmail{

	private static final String EMAIL_PATTERN = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	/**
	 * @param args
	 */
	public static ArrayList<String> getFromString(String str){

		ArrayList<String> emailList = new ArrayList<String>();
		Pattern p = Pattern.compile(EMAIL_PATTERN);
		Matcher m = p.matcher(str);
		while(m.find())
		{
			emailList.add(m.group());
			//	        System.out.println(m.group());
		}
		return emailList;

	}


}
