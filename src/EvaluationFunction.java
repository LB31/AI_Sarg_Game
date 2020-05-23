
public class EvaluationFunction {
	// Token amounts
	public float a; // tokenAmountOwn
	public float b; // token amount enemies
	
	// Scores
	public float c; // scoreOwn
	public float d; // score enemies
	
	// Tokens in a row
	public float e; // inRowOwn
	public float f; // inRowPlayer2
	public float g; // inRowPlayer3
	
	// Distance to score
	public float h; // only own
	
	/*
	 * 0 = token amount own
	 * 1 = token amount enemies
	 * 2 = score own
	 * 3 = score enemies
	 * 4 = own distance to score
	 */
	public float[] cs;

	public EvaluationFunction(float a, float b, float c, float d, /* float e, float f, float g, */ float h) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
//		this.e = e;
//		this.f = f;
//		this.g = g;
		this.h = h;
	}
	
	public EvaluationFunction(float[] evaVals) {
		cs = evaVals;
	}
	

	
	
	
	
	
}
