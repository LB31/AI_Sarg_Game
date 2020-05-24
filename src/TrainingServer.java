import java.io.IOException;

import lenz.htw.sarg.Server;

public class TrainingServer implements Runnable {

	private int winner = -10;
	
	EvaluationFunction[] evas;
	int evaIndex;
	
	public TrainingServer(EvaluationFunction[] evas, int evaIndex) {
		this.evas = evas;
		this.evaIndex = evaIndex;
	}
	
	public int getWinner() {
		return winner;
	}


	@Override
	public void run() {
		for (int i = evaIndex; i < evaIndex+3; i++) {
			try {
				new Thread(new ClientSarg("Player " + i, evas[i])).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		winner = Server.runOnceAndReturnTheWinner(4);
		System.err.println(winner + " winner of game " + evaIndex);
		
	}

}
