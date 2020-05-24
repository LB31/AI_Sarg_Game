import java.io.IOException;
import java.util.Arrays;

import lenz.htw.sarg.Server;

public class TrainingServer implements Runnable {

	private int winner = -10;
	private int secondBest = -10;

	EvaluationFunction[] evas;
	int evaIndex;

	ClientSarg[] sargFamily = new ClientSarg[3];

	public TrainingServer(EvaluationFunction[] evas, int evaIndex) {
		this.evas = evas;
		this.evaIndex = evaIndex;
	}

	public int getWinner() {
		return winner;
	}

	public int getSecondBest() {
		return secondBest;
	}

	@Override
	public void run() {

		for (int i = evaIndex; i < evaIndex + 3; i++) {
			try {
				sargFamily[i - evaIndex] = new ClientSarg("Player " + i, evas[i]);
				new Thread(sargFamily[i - evaIndex]).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		winner = Server.runOnceAndReturnTheWinner(4);
		
		winner = getBest(sargFamily[0].getScores());
		secondBest = getSecondBest(sargFamily[0].getScores());
		
		winner = getEvaNum(winner);
		secondBest = getEvaNum(secondBest);
		

		System.err.println(winner + " eva winner");
		System.err.println(secondBest + " eva second best");

	}
	
	private int getEvaNum(int index) {
		int evaNum = 0;
		for (int j = 0; j < sargFamily.length; j++) {
			if (index == sargFamily[j].getPlayerNumber()) {
				evaNum = j + evaIndex;
				break;
			}
		}
		
		return evaNum;
	}

	private int getSecondBest(int[] arr) {
		int scoreOwner = -1, first = -10, second = -10;

		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > first) {
				second = first;
				if (second >= 0)
					scoreOwner = i - 1;
				first = arr[i];
			} else if (arr[i] > second && arr[i] != first) {
				second = arr[i];
				scoreOwner = i;
			}
		}
		return scoreOwner;
	}

	public int getBest(int[] array) {
		int largest = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > array[largest])
				largest = i;
		}
		return largest; // position of the first largest found
	}

}
