package test;

public class Main {
	
	static class G {
		int h(int x) {
			System.out.println("h(" + x + ")");
			return x;
		}
	}
	
	static G g() {
		System.out.println("g");
		return new G();
	}
	
	static int f(int a, int b) {
		System.out.println("f(" + a + ", " + b + ")");
		return a + b;
	}
	
	static int x() {
		System.out.println("x");
		return 3;
	}
	
	static int y() {
		System.out.println("y");
		return -1;
	}
	
	public static void main(String[] args) {
		int a = f(g().h(x()),y());
	}

}
