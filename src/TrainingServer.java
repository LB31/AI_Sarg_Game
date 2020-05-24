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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		winner = Server.runOnceAndReturnTheWinner(4);
		if (winner == 0) // for the 0 bug
			winner = 2;
		winner = sargFamily[winner - 1].getPlayerNumber() + evaIndex;
		secondBest = getSecondBest(sargFamily[0].getScores()) + evaIndex;

		System.out.println(Arrays.toString(sargFamily[0].getScores()) + " scores");
		System.err.println(getSecondBest(sargFamily[0].getScores()) + " second score owner");

		System.err.println(winner + " winner of game " + evaIndex);
		for (int j = 0; j < sargFamily.length; j++) {
			System.err.println(sargFamily[j].getPlayerNumber() + " playernumber for eva" + (j + evaIndex));
		}

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
