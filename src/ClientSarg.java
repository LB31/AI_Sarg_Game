import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lenz.htw.sarg.Move;
import lenz.htw.sarg.net.NetworkClient;

public class ClientSarg implements Runnable {
	private String playerName;

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

	@Override
	public void run() {
		NetworkClient nc;
		try {
			nc = new NetworkClient("127.0.0.1", playerName, ImageIO.read(new File("./bilder/phoenix.png")));

			nc.getTimeLimitInSeconds();

			nc.getExpectedNetworkLatencyInMilliseconds();

			nc.getMyPlayerNumber(); // 0 = rot, 1 = grün, 2 = blau
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
