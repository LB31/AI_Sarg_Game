import java.io.IOException;
import java.util.Arrays;

public class Connector {
	static int[] wins = new int[9];
	static int[] secondBests = new int[9];

	static long waitBeforeStart = 2000; // milliseconds
	
	static EvaluationFunction[] evas;

	public static void main(String[] args) throws IOException, InterruptedException {
		assignEvas();

		for (int i = 0; i < 1; i++) {
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
			
			int winner1 = ts1.getWinner();
			int winner2 = ts2.getWinner();
			int winner3 = ts3.getWinner();

			wins[winner1]++;
			wins[winner2]++;
			wins[winner3]++;

		}
		
		System.out.println(Arrays.toString(wins) + " wins");
		getTopThree();
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
	
	static void assignEvas() {
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
		
		evas = new EvaluationFunction[] { eva0, eva1, eva2, eva3, eva4, eva5, eva6, eva7, eva8 };

	}

	static void getTopThree() {
		EvaluationFunction[] neoEvas = new EvaluationFunction[9];
		
		int highest = wins[0];
		int highestIndex = 0;
		for (int i = 1; i < wins.length; i++) {
			if(i % 3 == 0) { // the next round of clients
				neoEvas[i-3] = evas[highestIndex];
				System.out.println("highestIndex " + highestIndex);
				highest = wins[i];
				highestIndex = i;
				
				continue;
			}
			if(wins[i] > highest) {
				highest = wins[i]; 
				highestIndex = i;
			}
			if(i == wins.length - 1) { // last round
				neoEvas[i-2] = evas[highestIndex];
				System.out.println("highestIndex " + highestIndex);
			}
			
		}
	}
}
