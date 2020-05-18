import java.util.ArrayList;
import java.util.List;

public class PlayingField {

	public int[][] playfield = new int[9][9];
	public List<Token> tokenPositions = new ArrayList<Token>();
	public int[] scores = new int[3];
	
	public PlayingField(int[][] playfield, List<Token> tokenPositions, int[] scores) {
		this.playfield = playfield;
		this.tokenPositions = tokenPositions;
		this.scores = scores;
	}
	
	public PlayingField() {};
	
}
