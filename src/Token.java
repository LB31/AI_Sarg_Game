public class Token {
	public int x;
    public int y;
    public int owner;
    public boolean mine;
    
    public Token(int x, int y) {
    	this.x = x;
    	this.y = y;
    }
    
    public Token(int x, int y, int owner, int mainPlayer) {
    	this.x = x;
    	this.y = y;
    	this.owner = owner;
    	if(owner == mainPlayer)
    		mine = true;
    }
    
}
