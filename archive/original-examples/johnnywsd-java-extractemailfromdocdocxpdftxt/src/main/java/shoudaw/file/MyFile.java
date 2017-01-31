package shoudaw.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class MyFile {
	private String pathStr="";
	public MyFile(String pathStr){
		this.pathStr = pathStr;
	}

	public void overwrite(String str){
		PrintStream p = null;
		try{
			File file = new File(pathStr);
			if(!file.exists())
				file.createNewFile();
			FileOutputStream out=new FileOutputStream(pathStr);
			p=new PrintStream(out);
			p.println(str);
		} catch ( IOException e){
			e.printStackTrace();
		}finally{
			p.close();
		}
	}
	public String loadAllAsString3(){
		try {
			FileReader fr = new FileReader(pathStr);//����FileReader����������ȡ�ַ���
			BufferedReader br = new BufferedReader(fr);    //����ָ���ļ�������
			String myreadline;    //����һ��String���͵ı���,����ÿ�ζ�ȡһ��
			//            myreadline = br.readLine()+ "\n";//��ȡһ��
			myreadline = br.readLine();//��ȡһ��

			while (br.ready()) {
				//                myreadline += br.readLine()+"\n";//��ȡһ��
				myreadline += br.readLine();//��ȡһ��
			}
			br.close();
			fr.close();
			return myreadline;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String loadAllAsString2(){
		try {
			FileReader fr = new FileReader(pathStr);//����FileReader����������ȡ�ַ���
			BufferedReader br = new BufferedReader(fr);    //����ָ���ļ�������
			String myreadline;    //����һ��String���͵ı���,����ÿ�ζ�ȡһ��
			myreadline = br.readLine();//��ȡһ��
			while (br.ready()) {
				myreadline += br.readLine();//��ȡһ��
			}
			br.close();
			fr.close();
			return myreadline;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String[] getAllFileName(String path){
		
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		if (!file.isDirectory()) {
			return null;
		}
		String[] fileList = file.list(); 
		for (int i = 0; i < fileList.length; i++) {
			fileList[i] = file.getAbsolutePath().toString()+"/"+fileList[i];
		}
		return fileList;
	}
	
	public static String loadAllAsString(String pathStr){
		try {
//			FileReader fr = new FileReader(pathStr);//����FileReader����������ȡ�ַ���
			InputStreamReader fr = new InputStreamReader(new FileInputStream(pathStr),"UTF-8"); 
			BufferedReader br = new BufferedReader(fr);    //����ָ���ļ�������
			String myreadline;    //����һ��String���͵ı���,����ÿ�ζ�ȡһ��
			//            myreadline = br.readLine()+ "\n";//��ȡһ��
			myreadline = br.readLine();//��ȡһ��

			while (br.ready()) {
				//                myreadline += br.readLine()+"\n";//��ȡһ��
				myreadline += br.readLine();//��ȡһ��
			}
			br.close();
			fr.close();
			return myreadline;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
