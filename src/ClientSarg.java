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
	private int[][] tokenPositions = new int[9][9];
	private List<Vector2D> positionsOwnTokens = new ArrayList<Vector2D>();
	private List<Vector2D> positionsPlayer2 = new ArrayList<Vector2D>();
	private List<Vector2D> positionsPlayer3 = new ArrayList<Vector2D>();
	private int playerNumber2;
	private int playerNumber3;
	

	public ClientSarg(String name) throws IOException {
		playerName = name;

	}

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
			
			Vector2D posRed = new Vector2D(i, 0);
			Vector2D posGreen = new Vector2D(i, 4+i);
			Vector2D posBlue = new Vector2D(8, 4+i);
			
			if(playerNumber == 0) {
				positionsOwnTokens.add(posRed);
				positionsPlayer2.add(posGreen);
				positionsPlayer3.add(posBlue);
				playerNumber2 = 1;
				playerNumber3 = 2;
			}
			else if(playerNumber == 1) {
				positionsOwnTokens.add(posGreen);
				positionsPlayer2.add(posRed);
				positionsPlayer3.add(posBlue);
				playerNumber2 = 0;
				playerNumber3 = 2;
			}		
			else {
				positionsOwnTokens.add(posBlue);
				positionsPlayer2.add(posRed);
				positionsPlayer3.add(posGreen);
				playerNumber2 = 0;
				playerNumber3 = 1;
			}
				
		}
	}

	@Override
	public void run() {

		try {
			nc = new NetworkClient("127.0.0.1", playerName, ImageIO.read(new File("./bilder/phoenix.png")));
			
			initialize();

			// Debug
			System.out.println("Timelimit " + nc.getTimeLimitInSeconds());
			System.out.println("getExpectedNetworkLatencyInMilliseconds " + nc.getExpectedNetworkLatencyInMilliseconds());
			for (int[] row : tokenPositions) {
				System.out.println(Arrays.toString(row)); 
			}
			System.out.println(positionsOwnTokens.get(4).x);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		update();
	}

	private void update() {
		while (true) {
			Move receivedMove = nc.receiveMove();
			if (receivedMove == null) {
				// ich bin dran
				Move move = calculateMove();
				nc.sendMove(move);
			} else {
				updatePlayfield(receivedMove);
			}
		}
	}
	

	private Move calculateMove() {
		Move move = null;
		
		int randomNum = ThreadLocalRandom.current().nextInt(0, positionsOwnTokens.size());
		Vector2D token = positionsOwnTokens.get(randomNum);
		
		move = new Move((int)token.x, (int)token.y);
		
		return move;
	}
	
	private void updatePosition(Vector2D startPos, List<Vector2D> player) {
		
	}
	
	private void updatePlayfield(Move move) {
		int playerNumber = tokenPositions[move.x][move.y];
		tokenPositions[move.x][move.y] = 0;
		if(playerNumber == 1) { // red
			tokenPositions[move.x][move.y + 1] = 1;
			tokenPositions[move.x + 1][move.y + 1] = 1;
		}
		else if(playerNumber == 2) { // green
			tokenPositions[move.x + 1][move.y] = 2;
			tokenPositions[move.x][move.y - 1] = 2;
		}
		else { // blue
			tokenPositions[move.x - 1][move.y - 1] = 3;
			tokenPositions[move.x - 1][move.y] = 3;
		}
		
		if(playerNumber-1 == playerNumber) {
			
		}
		
	}

}
