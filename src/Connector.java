import java.io.IOException;
import java.util.Arrays;

public class Connector {
	static int[] wins = new int[9];
	
	// Debug
	static String winners = "";

	static long waitBeforeStart = 2000; // milliseconds

	public static void main(String[] args) throws IOException, InterruptedException {

		
		EvaluationFunction eva0 = new EvaluationFunction(new float[] { 
				2, // own tokens
				8, // enemy tokens
				8, // own score
				1, // enemy scores
				0.2f // own distances to win
		});
		EvaluationFunction eva1 = new EvaluationFunction(new float[] { 
				5, // own tokens
				9, // enemy tokens
				4, // own score
				9, // enemy scores
				0.4f // own distances to win
		});
		EvaluationFunction eva2 = new EvaluationFunction(new float[] { 
				2, // own tokens
				10, // enemy tokens
				3, // own score
				3, // enemy scores
				0.7f // own distances to win
		});

		EvaluationFunction eva3 = new EvaluationFunction(new float[] { 
				6, // own tokens
				8, // enemy tokens
				4, // own score
				7, // enemy scores
				0.5f // own distances to win
		});
		EvaluationFunction eva4 = new EvaluationFunction(new float[] { 
				5, // own tokens
				2, // enemy tokens
				6, // own score
				5, // enemy scores
				0.8f // own distances to win
		});
		EvaluationFunction eva5 = new EvaluationFunction(new float[] { 
				2, // own tokens
				9, // enemy tokens
				6, // own score
				8, // enemy scores
				0.7f // own distances to win
		});

		EvaluationFunction eva6 = new EvaluationFunction(new float[] { 
				2, // own tokens
				5, // enemy tokens
				8, // own score
				12, // enemy scores
				0.2f // own distances to win
		});
		EvaluationFunction eva7 = new EvaluationFunction(new float[] { 
				5, // own tokens
				4, // enemy tokens
				2, // own score
				2, // enemy scores
				0.5f // own distances to win
		});
		EvaluationFunction eva8 = new EvaluationFunction(new float[] { 
				2, // own tokens
				10, // enemy tokens
				5, // own score
				8, // enemy scores
				0.3f // own distances to win
		});

		EvaluationFunction[] evas = new EvaluationFunction[] { eva0, eva1, eva2, eva3, eva4, eva5, eva6, eva7, eva8 };

		for (int i = 0; i < 3; i++) {
			TrainingServer ts1 = new TrainingServer(evas, 0);
			Thread a = new Thread(ts1);
			a.start();
			Thread.sleep(waitBeforeStart);
			TrainingServer ts2 = new TrainingServer(evas, 3);
			Thread b = new Thread(ts2);
			b.start();
			Thread.sleep(waitBeforeStart);
			TrainingServer ts3 = new TrainingServer(evas, 6);
			Thread c = new Thread(ts3);
			c.start();

			a.join();
			b.join();
			c.join();
			System.out.println(ts1.getWinner() + " one");
			System.out.println(ts2.getWinner() + " two");
			System.out.println(ts3.getWinner() + " three");

//			scores[ts1.getWinner()]++;
//			scores[ts2.getWinner()+3]++;
//			scores[ts3.getWinner()+6]++;
			
			winners += ts1.getWinner() + " | " + ts2.getWinner() + " | " + ts3.getWinner() + " | ";
		}
		
		System.out.println(winners);
//		System.out.println(Arrays.toString(scores) + " winners");

//		int winner = Server.runOnceAndReturnTheWinner(4);
//		System.out.println(winner + " winner");
//		
//		for (int i = 0; i < 3; i++) {
//			System.out.println(evas[i].cs[i] + " eva a");
//			new Thread(new ClientSarg("Player " + i, evas[i])).start();
//		}
//		
//		winner = Server.runOnceAndReturnTheWinner(4);
//		System.out.println(winner + " winner");
	}

}
