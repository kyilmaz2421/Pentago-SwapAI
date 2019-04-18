package student_player;

import pentago_swap.PentagoBoardState;

public class MoveData {
	private int x;
	private int y;
	private int qA;
	private int qB;
	private int score;
	private int strategy;
	private PentagoBoardState board;
	
	public MoveData(int y,int x, int qA, int qB, int score, int strategy,PentagoBoardState board){
		this.x = x;
		this.y = y;
		this.qA = qA;
		this.qB = qB;
		this.score = score;
		this.strategy = strategy;
		this.board = board;
	}
	 public void setCoords(int y , int x){
		 this.x =x;
		 this.y = y;
	 }
	public PentagoBoardState getBoard(){
		return board;
	}
	
	public int[] getCoords(){
		int[] arr = {y,x};
		return arr;
	}
	
	public int getScore(){
		return score;
	}
	public int getqA(){
		return qA;
	}
	public int getqB(){
		return qB;
	}
	public int getStrategy(){
		return strategy;
	}
	public void setScore(int i){
		score = i;
	}
}
