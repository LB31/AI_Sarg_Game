
public class Test {
	public String name;
	
	public static void main(String[] args) {
		Test test1 = new Test();
		test1.name = "Peter";
		
		Buh buh1 = new Buh(test1);
		buh1.thisTest.name = "Ingrid";
		
		System.out.println(test1.name);
		System.out.println(buh1.thisTest.name);

	}

}

