import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Connector {

	public static void main(String[] args) throws IOException {

		System.out.println("start");
		for (int i = 0; i < 3; i++) {
			System.out.println(i);
			new Thread(new ClientSarg("Player " + i)).start();
		}
	}

}
