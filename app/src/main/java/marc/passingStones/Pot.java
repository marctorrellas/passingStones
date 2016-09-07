package marc.passingStones;

public class Pot {
	public static int longPot=20, sepPots =20;
	protected int stones;
	private float x;
	private float y;
	private int player;  // 0 or 1
    public int incrementStones(){
        this.stones =this.stones +1;
        return stones;
    }
    public int getStones(){
    	return stones;
    }
    public int emptyPot(){
        int tmp = this.stones;
    	this.stones =0;
        return tmp; 
    }
    public int getPlayer(){
    	return player;
    }
    
    public float getX(){
    	return x;
    }
    
    public float getY(){
    	return y;
    }
    
    public void setXY(float X, float Y){
    	this.x=X;
    	this.y=Y;
    }
    
    
    public Pot(int player, int stones){
    	this.player = player;
    	this.stones = stones;
    	this.x=0f;
    	this.y=0f;
    }
}
