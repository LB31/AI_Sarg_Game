import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import lenz.htw.sarg.Move;
import lenz.htw.sarg.net.NetworkClient;

public class ClientSarg implements Runnable {
	private NetworkClient nc;

	private String playerName;
	private int playerNumber;
	
	// Test
	private int lastX;
	private int lastY;
	private int[][] tokenPositions = new int[9][9];
	private List<Vector2D> ownTokenPositions = new ArrayList<Vector2D>();

	

	public ClientSarg(String name) throws IOException {
		playerName = name;

	}
//
//	        // initialisieren... z.B. Spielbrett
//
//	        
//
//	        while (true) {
//	            Move receiveMove = nc.receiveMove();
//	            if (receiveMove == null) {
//	                // ich bin dran
//	                // Move move = findeCleverenZug();
//	                nc.sendMove(move);
//	            } else {
//	                // integriereZugInSpielbrett(move);
//	            }
//	        }
//	    }
	
	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = rot, 1 = grün, 2 = blau
		System.out.println(playerNumber + " number");
		
		for (int i = 0; i < 5; i++) {
			// red
			tokenPositions[i][0] = 1;
			// green
			tokenPositions[i][4+i] = 2;
			// blue
			tokenPositions[8][4+i] = 3;
			
			if(playerNumber == 0)
				ownTokenPositions.add(new Vector2D(i, 0));
			else if(playerNumber == 1)
				ownTokenPositions.add(new Vector2D(i, 4+i));
			else
				ownTokenPositions.add(new Vector2D(8, 4+i));
		}
	}

	@Override
	public void run() {

		try {
			


			
			nc = new NetworkClient("127.0.0.1", playerName, ImageIO.read(new File("./bilder/phoenix.png")));

			nc.getTimeLimitInSeconds();

			nc.getExpectedNetworkLatencyInMilliseconds();

			initialize();

			for (int[] row : tokenPositions) {
				System.out.println(Arrays.toString(row)); 
			}
			
			System.out.println(ownTokenPositions.get(4).x);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		update();
	}

	private void update() {
		while (true) {
			Move receiveMove = nc.receiveMove();
			if (receiveMove == null) {
				// ich bin dran
				Move move = calculateMove();
				nc.sendMove(move);
			} else {
				// integriereZugInSpielbrett(move);
			}
		}
	}

	private Move calculateMove() {
		Move move;
		switch (playerNumber) {
		case 0: {
			int y = 0;

			move = new Move(0, y + lastY);
			lastY++;
			break;
		}
		case 1: {
			int x = 4;

			move = new Move(x + lastX, 8);
			lastX++;
			break;
		}
		case 2: {
			int x = 8;
			int y = 4;

			move = new Move(x + lastX, y + lastY);
			lastX--;
			lastY--;
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + playerNumber);
		}
		return move;
	}

}
