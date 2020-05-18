import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Connector {

	public static void main(String[] args) throws IOException {

		EvaluationFunction[] evas = new EvaluationFunction[] {
				new EvaluationFunction(3, 2, 2, 4, 3, 3, 2, 1.5f, 1.5f, 2),
				new EvaluationFunction(5, 4, 4, 2, 1, 1, 3, 2, 2, 5),
				new EvaluationFunction(5, 4, 4, 2, 1, 1, 3, 2, 2, 5),
		};
		
		for (int i = 0; i < 3; i++) {
			System.out.println(i);
			new Thread(new ClientSarg("Player " + i, evas[i])).start();
		}
	}

}
