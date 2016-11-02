package shoudaw.file;

public class testMain {
	public static void main(String[] args){
//		MyFile mf = new MyFile("./section-001.html");
//		mf.overwrite("1\n01010111101010100001\n0011001110101");
//		String tmp = mf.loadAllAsString();
//		String[] lst = mf.getAllFileName("./");
//		System.out.print(tmp);
		String tmp = MyFile.loadAllAsString("/home/johnny/workspace/eclipse/java/sendEmail/file-list/section-001.html");
		System.out.print(tmp);
			
//		for(int i=0;i<lst.length;i++){
//			System.out.println(lst[i]);
//		}
	}
}
