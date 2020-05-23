import lenz.htw.sarg.Server;

public class ServerTest {

	public static void main(String[] args) {
		int winner = Server.runOnceAndReturnTheWinner(4000);
		System.out.println(winner);
	}

}
