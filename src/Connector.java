import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Connector {
	static int[] wins = new int[9];
	static int[] secondBests = new int[9];

	static long waitBeforeStart = 2000; // milliseconds
	static float coefficientMax = 10;

	static int matchAmount = 10;
	static int generationAmount = 10;

	static List<EvaluationFunction> evas;
	static List<EvaluationFunction> neoEvas;
	
	// debug
	static long startTime;
	static long endTime;

	public static void main(String[] args) throws IOException, InterruptedException {
		assignEvas();
		startTime = System.currentTimeMillis();
		for (int i = 0; i < generationAmount; i++) {

			for (int j = 0; j < matchAmount; j++) {
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

				int second1 = ts1.getSecondBest();
				int second2 = ts2.getSecondBest();
				int second3 = ts3.getSecondBest();

				wins[winner1]++;
				wins[winner2]++;
				wins[winner3]++;

				secondBests[second1]++;
				secondBests[second2]++;
				secondBests[second3]++;

			}

			// keep the veterans of the last generation
			if(i == generationAmount - 1) continue;
			
			neoEvas = new ArrayList<>();
			

			List<EvaluationFunction> winners = getTopThree(wins);
			// Mutate the second best evas
			List<EvaluationFunction> seconds = getTopThree(secondBests);
			for (EvaluationFunction eva : seconds) {
				mutate(eva);
			}
			// Produce new children from the winners
			List<EvaluationFunction> children = new ArrayList<>();
			children.add(produceMeanChild(winners.get(0).cs, winners.get(1).cs));
			children.add(produceMeanChild(winners.get(0).cs, winners.get(2).cs));
			children.add(produceMeanChild(winners.get(1).cs, winners.get(2).cs));

			// Create new generation
			for (int k = 0; k < 3; k++) {
				neoEvas.add(winners.get(k));
				neoEvas.add(seconds.get(k));
				neoEvas.add(children.get(k));
			}
			
			evas = neoEvas;
			
			wins = new int[9];
			secondBests = new int[9];

		}
	
		System.out.println(Arrays.toString(wins) + " wins");
		System.err.println(Arrays.toString(secondBests) + " secondBests");
		for (EvaluationFunction eva : evas) {
			System.out.println(Arrays.toString(eva.cs) + " neo eva");
		}
		List<EvaluationFunction> winners = getTopThree(wins);
		for (EvaluationFunction eva : winners) {
			System.err.println(Arrays.toString(eva.cs) + " absolute winner");
		}
		
		
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) * 0.001 + " seconds for " + generationAmount + " generations a " + matchAmount + " matches");

	}

	static void assignEvas() {
		// the token brain 
		EvaluationFunction eva0 = new EvaluationFunction(new float[] { 
				8, // own tokens
				7, // enemy tokens
				4, // own score
				3, // enemy scores
				0.2f // own distances to win
		});
		// the score brain
		EvaluationFunction eva1 = new EvaluationFunction(new float[] { 
				4, // own tokens
				3, // enemy tokens
				8, // own score
				7, // enemy scores
				0.4f // own distances to win
		});
		// crazy random monkey
		EvaluationFunction eva2 = new EvaluationFunction(new float[] { 
				9, // own tokens
				1, // enemy tokens
				8, // own score
				2, // enemy scores
				0.8f // own distances to win
		});
		// the token brain 
		EvaluationFunction eva3 = new EvaluationFunction(new float[] { 
				7, // own tokens
				9, // enemy tokens
				2, // own score
				4, // enemy scores
				0.1f // own distances to win
		});
		// the score brain
		EvaluationFunction eva4 = new EvaluationFunction(new float[] { 
				3, // own tokens
				4, // enemy tokens
				7, // own score
				9, // enemy scores
				0.9f // own distances to win
		});
		// crazy donkey
		EvaluationFunction eva5 = new EvaluationFunction(new float[] { 
				2, // own tokens
				9, // enemy tokens
				3, // own score
				10, // enemy scores
				0.3f // own distances to win
		});
		// the token brain 
		EvaluationFunction eva6 = new EvaluationFunction(new float[] { 
				7, // own tokens
				7, // enemy tokens
				2, // own score
				2, // enemy scores
				0.6f // own distances to win
		});
		// the score brain
		EvaluationFunction eva7 = new EvaluationFunction(new float[] { 
				5, // own tokens
				4, // enemy tokens
				7, // own score
				9, // enemy scores
				0.9f // own distances to win
		});
		// crazy american president
		EvaluationFunction eva8 = new EvaluationFunction(new float[] { 
				10, // own tokens
				9, // enemy tokens
				8, // own score
				7, // enemy scores
				0.6f // own distances to win
		});

//		evas = new EvaluationFunction[] { eva0, eva1, eva2, eva3, eva4, eva5, eva6, eva7, eva8 };
		evas = new ArrayList<>() {
			{
				add(eva0);
				add(eva1);
				add(eva2);
				add(eva3);
				add(eva4);
				add(eva5);
				add(eva6);
				add(eva7);
				add(eva8);
			}
		};

	}


	
	static void mutate(EvaluationFunction eva) {
		float changer = 0.3f;
		for (int i = 0; i < eva.cs.length; i++) {
			float c = eva.cs[i];
			if (c >= coefficientMax * changer)
				eva.cs[i] += (coefficientMax - c) * changer;
			else
				eva.cs[i] -= c * changer;
		}

	}

	static EvaluationFunction produceMeanChild(float[] mother, float[] notTheMother) {
		float changer = 0.5f;
		EvaluationFunction eva = new EvaluationFunction(new float[] { 
				(mother[0] + notTheMother[0]) * changer, // own // tokens																						
				(mother[1] + notTheMother[1]) * changer, // enemy tokens
				(mother[2] + notTheMother[2]) * changer, // own score
				(mother[3] + notTheMother[3]) * changer, // enemy scores
				(mother[4] + notTheMother[4]) * changer // own distances to win
		});
		return eva;
	}

	static List<EvaluationFunction> getTopThree(int[] arr) {
		List<EvaluationFunction> tempEva = new ArrayList<>();
		int highest = arr[0];
		int highestIndex = 0;
		for (int i = 1; i < arr.length; i++) {
			if (i % 3 == 0) { // the next round of clients
//				neoEvas[i - 3] = evas[highestIndex];
				tempEva.add(evas.get(highestIndex));
				System.out.println("highestIndex " + highestIndex);
				highest = arr[i];
				highestIndex = i;

				continue;
			}
			if (arr[i] > highest) {
				highest = arr[i];
				highestIndex = i;
			}
			if (i == arr.length - 1) { // last round
//				neoEvas[i - 2] = evas[highestIndex];
				tempEva.add(evas.get(highestIndex));
				System.out.println("highestIndex " + highestIndex);
			}
		}
		return tempEva;
	}

}
