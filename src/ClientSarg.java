import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import lenz.htw.sarg.Move;
import lenz.htw.sarg.net.NetworkClient;

public class ClientSarg implements Runnable {
	private NetworkClient nc;

	private String playerName;
	private int playerNumber;
	
	// Test
	private int[][] playfield = new int[9][9];
	private List<Token> tokenPositions = new ArrayList<Token>();

	

	public ClientSarg(String name) throws IOException {
		playerName = name;

	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = rot, 1 = grün, 2 = blau
		System.out.println(playerNumber + " number");
		
		for (int i = 0; i < 5; i++) {
			// red
			playfield[i][0] = 1;
			// green
			playfield[i][4+i] = 2;
			// blue
			playfield[8][4+i] = 3;
			
			Token posRed = new Token(i, 0, 0, playerNumber);
			Token posGreen = new Token(i, 4+i, 1, playerNumber);
			Token posBlue = new Token(8, 4+i, 2, playerNumber);
			
			tokenPositions.add(posRed);
			tokenPositions.add(posGreen);
			tokenPositions.add(posBlue);
				
		}
		
//		if(playerNumber == 0) {
//			for (Token t : tokenPositions) {
//				System.out.println("X:" + t.x + " Y:" + t.y);
//			}
//			System.out.println(tokenPositions.size() + " Size");
//		}
		
		
	}

	@Override
	public void run() {

		try {
			nc = new NetworkClient("127.0.0.1", playerName, ImageIO.read(new File("./bilder/phoenix.png")));
			
			initialize();

			// Debug
//			System.out.println("Timelimit " + nc.getTimeLimitInSeconds());
//			System.out.println("getExpectedNetworkLatencyInMilliseconds " + nc.getExpectedNetworkLatencyInMilliseconds());
//			for (int[] row : playfield) {
//				System.out.println(Arrays.toString(row)); 
//			}
//			System.out.println(tokenPositions.get(4).x);
			
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
		Token[] myTokens = tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
		
		
		
		int randomNum = ThreadLocalRandom.current().nextInt(0, myTokens.length);
		Token token = myTokens[randomNum];
		
//		System.out.println("Anzahl meiner Token: " + myTokens.length);
//		System.out.println("Gewählter Token: " + randomNum);
		
		move = new Move((int)token.x, (int)token.y);
		System.out.println("Kommender move: " + move.x + " X " + move.y + " Y");
		return move;
	}
	
	private void updatePosition(Vector2D startPos, List<Vector2D> player) {
		
	}
	
	private void updatePlayfield(Move move) {
		Optional<Token> token = tokenPositions.stream().filter(t -> t.x == move.x && t.y == move.y).findFirst();
		System.out.println(token.isPresent() + " element found");
		tokenPositions.remove(token.get());
		
		int ownerNumber = playfield[move.x][move.y] - 1;
		playfield[move.x][move.y] = 0;
		if(ownerNumber == 0) { // red
			playfield[move.x][move.y + 1] = 1;
			playfield[move.x + 1][move.y + 1] = 1;
			tokenPositions.add(new Token(move.x, move.y + 1, ownerNumber, playerNumber));
			tokenPositions.add(new Token(move.x + 1, move.y + 1, ownerNumber, playerNumber));
		}
		else if(ownerNumber == 1) { // green
			playfield[move.x + 1][move.y] = 2;
			playfield[move.x][move.y - 1] = 2;
			tokenPositions.add(new Token(move.x + 1, move.y, ownerNumber, playerNumber));
			tokenPositions.add(new Token(move.x, move.y - 1, ownerNumber, playerNumber));
		}
		else { // blue
			playfield[move.x - 1][move.y - 1] = 3;
			playfield[move.x - 1][move.y] = 3;
			tokenPositions.add(new Token(move.x - 1, move.y - 1, ownerNumber, playerNumber));
			tokenPositions.add(new Token(move.x - 1, move.y, ownerNumber, playerNumber));
		}
		
		
		
	}

}
