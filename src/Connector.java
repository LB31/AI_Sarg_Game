import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Connector {

	public static void main(String[] args) throws IOException {

		EvaluationFunction eva0 = new EvaluationFunction(0, 0, 0, 0, 0) {
			{
				a = 2; // own tokens
				b = 8; // enemy tokens
				c = 8; // own score
				d = 12; // enemy scores
				h = 0.2f; // own distances to win
			}
		};
		
		EvaluationFunction eva1 = new EvaluationFunction(0, 0, 0, 0, 0) {
			{
				a = 5; // own tokens
				b = 5; // enemy tokens
				c = 10; // own score
				d = 2; // enemy scores
				h = 0; // own distances to win
			}
		};
		
		EvaluationFunction eva2 = new EvaluationFunction(0, 0, 0, 0, 0) {
			{
				a = 2; // own tokens
				b = 10; // enemy tokens
				c = 2; // own score
				d = 10; // enemy scores
				h = 2; // own distances to win
			}
		};

		EvaluationFunction[] evas = new EvaluationFunction[] {eva0, eva1, eva2};

		for (int i = 0; i < 3; i++) {
			System.out.println(evas[i].a);
			new Thread(new ClientSarg("Player " + i, evas[i])).start();
			
		}
	}

}
