import java.io.IOException;
import lenz.htw.sarg.Server;

public class Connector {

	public static void main(String[] args) throws IOException {

//		EvaluationFunction eva0 = new EvaluationFunction(0, 0, 0, 0, 0) {
//			{
//				a = 2; // own tokens
//				b = 8; // enemy tokens
//				c = 8; // own score
//				d = 12; // enemy scores
//				h = 0.2f; // own distances to win
//			}
//		};
//		
		EvaluationFunction eva0 = new EvaluationFunction(new float[] {
				2, // own tokens
				8, // enemy tokens
				8, // own score
				12, // enemy scores
				0.2f // own distances to win
				});
		EvaluationFunction eva1 = new EvaluationFunction(new float[] {
				5, // own tokens
				5, // enemy tokens
				10, // own score
				2, // enemy scores
				0.5f // own distances to win
				});
		EvaluationFunction eva2 = new EvaluationFunction(new float[] {
				2, // own tokens
				10, // enemy tokens
				3, // own score
				8, // enemy scores
				0.7f // own distances to win
				});


		EvaluationFunction[] evas = new EvaluationFunction[] {eva0, eva1, eva2};

		for (int i = 0; i < 3; i++) {
			System.out.println(evas[i].a + " eva a");
			new Thread(new ClientSarg("Player " + i, evas[i])).start();
		}
		
		int winner = Server.runOnceAndReturnTheWinner(4);
		System.out.println(winner + " winner");
	}

}
