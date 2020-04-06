package student_player;

import java.util.*;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import java.util.Random;


public class MyTools {

	private static boolean goalState = false;
	private static PentagoBoardState realBoardState;
	private static int playerNum;
	private static String studentPlayerPiece;
	private static String opponentPlayerPiece;
	private static boolean hadFirstTurn; 
	
    
    public static Move bestMove(PentagoBoardState state){
    	realBoardState = state;
    	playerNum = state.getTurnPlayer();
	   	if(playerNum==0){
	   		studentPlayerPiece = "w";
	   		opponentPlayerPiece = "b";
	   		hadFirstTurn = true;
    	}
	   	else{
	   		studentPlayerPiece = "b";
	   		opponentPlayerPiece = "w";
	   		hadFirstTurn = false;
	   	}
    	if(state.getTurnNumber()<2){
    		int pos = 0;
   			int[][] coord = {{1,1},{1,4},{4,1},{4,4}}; // y,x works not x,y for some reason
   			
   			boolean quadIsClear = false;
   			while(!quadIsClear){
   				quadIsClear = true;
   				for(int j=-1; j<2;j++){
	   				for(int i=-1; i<2;i++){
	   					if((!realBoardState.isPlaceLegal(new PentagoCoord(coord[pos][0]+j,coord[pos][1] +i)))){
	   						pos++;
	   						quadIsClear = false;
	   						break;
	   					}
	   				}
	   				if(!quadIsClear){
	   					break;
	   				}
   				}
	    	}
   			MoveData m = new MoveData(coord[pos][0],coord[pos][1],-1,-1,-1,-1,realBoardState);
    		return createMove(m);
    	}
    	int depth = 0;
    	if(state.getTurnNumber()<4){
    		depth = 4;
    	}
    	else{
    		depth = 6;
    	}
    	int k=0;
    	int[] bestResultVerification = new int[2];
    	Move[] bestMoveVerification = new Move[2];
    	int[] bestResult = new int[2];
    	Move[] bestMove = new Move[2];
    	boolean simulatingOpp = false, defence = false;
    	while(k<2){
	    	if(k==1){
	    		defence = true;
	    	}
	    	// (maximize, simplayer)
	    	ArrayList<MoveData> topMoves = moveSelector(realBoardState,simulatingOpp,defence);// 3rd bool is irrelevant (here)
	    	try{
	    		bestResultVerification[k]=topMoves.get(0).getScore();
	    		bestMoveVerification[k]= createMove(topMoves.get(0));	
	    	}
	    	catch(Exception e){
	    		bestResultVerification[k]=0;
	    		bestMoveVerification[k]= null;	
	    	}	
		   	 
	    		for(MoveData m: topMoves){
		   		 PentagoBoardState safeState = (PentagoBoardState) realBoardState.clone();
		   		 safeState.processMove((PentagoMove) createMove(m));
		   		 if(safeState.gameOver()||m.getScore()>=13){
		   			 if((m.getBoard().getTurnPlayer())==safeState.getWinner()||m.getScore()>=13){
				    	 return createMove(m);
				 	 }
		   			 else{
		   				 continue;
		   			 }
		   		 }
		   		int tempResult=0;	
		   		if(k==1){																// (maximize, simplayer)
		   			tempResult = alphaBeta(safeState,m,depth,Integer.MIN_VALUE,Integer.MAX_VALUE,true,false); //maximizing and 
		   		}
		   		else{
		   			tempResult = alphaBeta(safeState,m,depth,Integer.MIN_VALUE,Integer.MAX_VALUE,true,false); //maximizing and 
		   		}
		   		 if(bestResult[k]<tempResult){
		   			 bestResult[k] = tempResult;
		   			 bestMove[k] = createMove(m);
		   		 }
		   	 }
		   	 k++;
    	}
    	System.out.println("bestReslt D = "+ bestResult[1]+"bestReslt OFf = "+ bestResult[0]);
    	System.out.println("bestReslt D verified = "+ bestResultVerification[1]+"bestReslt OFF verified = "+ bestResultVerification[0]);
    	if((bestResult[0]<bestResultVerification[1]||bestResult[0]<bestResult[1])){ //offense < verified Defense
    			return bestMoveVerification[1];
    	}
    	if(bestResult[0]==bestResult[1]){
    		if(bestResultVerification[1]>=bestResultVerification[0]){
    			return bestMoveVerification[1];
    		}
    		else{
    			return bestMove[0];
    		}
    	}
    	if(bestResultVerification[0]>bestResult[0]){
			return bestMoveVerification[0];
		}
    	
    	return bestMove[0];
      }
    public static int alphaBeta(PentagoBoardState state,MoveData move, int depth,int alpha, int beta, boolean maximizingPlayer,boolean simulatingOpp){
 		int val;
 		if(depth==0||move.getScore()==13){ //or maybe state.gameOver()
 			return move.getScore();
 		}
 		if(maximizingPlayer){
 			val = Integer.MIN_VALUE;
 			ArrayList<MoveData> bestMoves = moveSelector(state,simulatingOpp,true); // third bool is currently irelavant
 			
 			for(MoveData m: bestMoves){
 				val = Math.max(val, alphaBeta(state, m,depth-1,alpha,beta,false,true)); //last bool is for simming opponent
 				alpha = Math.max(alpha, val);
 				if(alpha>=beta){
 					break;
 				}
 			}
 			return val;
 		}
 		else{
 			val = Integer.MAX_VALUE;
 			ArrayList<MoveData> bestMoves = moveSelector(state,simulatingOpp,true);
 			
 			for(MoveData m: bestMoves){
 				val = Math.min(val, alphaBeta(state, m,depth-1,alpha,beta,true,false));
 				beta = Math.min(beta, val);
 				if(alpha>beta){
 					break;
 				}
 			}
 			return val;
 		}
 	}
    
    public static HashMap<Integer,ArrayList<MoveData>> offensiveMove(PentagoBoardState boardState, boolean simulatingOpp){
    	if(simulatingOpp){
    		return horzDiagVertMoveFinder(opponentPlayerPiece, boardState, simulatingOpp,simulatingOpp);
    	}
    	else{
    		return horzDiagVertMoveFinder(studentPlayerPiece, boardState, simulatingOpp,simulatingOpp);
    	}
    }
    public static HashMap<Integer,ArrayList<MoveData>> defensiveMove(PentagoBoardState boardState, boolean simulatingOpp){
    	if(simulatingOpp){
    		return horzDiagVertMoveFinder(studentPlayerPiece, boardState, true,simulatingOpp);
    	}
    	else{
    		return horzDiagVertMoveFinder(opponentPlayerPiece, boardState, true,simulatingOpp);
    	} // the opponents best offensive move may constitute as our best defensive move
    }
    
	
    public static ArrayList<MoveData> moveSelector(PentagoBoardState boardState, boolean simulatingOpp, boolean defence){  
    	HashMap<Integer,ArrayList<MoveData>> moveMap = null;
    	if(!defence)  {
    		moveMap = offensiveMove(boardState, simulatingOpp); //offense
    	}
    	else{
    		moveMap = defensiveMove(boardState, simulatingOpp); //defense

    	}
	 	  ArrayList<MoveData> topMoves = new  ArrayList<MoveData>();
	 	  int topFinds = 0, i=13,counter = 3;
	 	  while((i>-1)&&(topMoves.size() < counter)){
		 	 if(moveMap.containsKey((Integer)i)){
					ArrayList<MoveData> arr = moveMap.get(i);
					if(topFinds==0){
		 			topMoves.addAll(arr);
		 			arr.clear();
		 			moveMap.put((Integer)i, arr);
		 			topFinds++;
		 		 }
				else if(!arr.isEmpty()){
				topMoves.add(arr.get(0));
				arr.remove(0);
				moveMap.put((Integer)i, arr);
				topFinds++;
		 			}
				 }
		 	 i--;
	 	  }
	 	  return topMoves;
	}


    public static int horzVertDiagScoreGuide(int score, int oppCount, boolean goal,boolean centredA, boolean centredB, boolean moveQuadBigger, boolean unbalanced, int section, boolean maximizingPlayer){
    	// unbalanced towards a quad
    	switch(score){ // can always assume the centre piece has no opponent
    		case 4:
    			if(centredA&&centredB){
    				if((section == 8)&&(oppCount==0)){
    					score = 0;}
    				else {score = 13;}}//4 on the board centered and max one opponent
    			else if(centredA||centredB){score=13;}
    			else if(((!centredA)&&(!centredB))){ // 2 uncentred on each side not very good easily blocked
    				if(oppCount==0){ score = 9;}// create a defensive rating on opponent
    				else{ score = 6;} 
				}
    			break;
    		case 3:	 //usually 3 on the board
    			if(((!centredA)&&centredB)||(centredA&&(!centredB))){
   					if(oppCount==0){ score = 12;} // must do move as huge oppurtunity but easily missed
       				else{ score = 11;} //gaining/blocking a centre is always valuable
   				}
    			else if((centredA&&centredB)){
    				if(oppCount==0){ score = 12;} 
        			else{ score = 11;} 
    			}
    			else if((!centredA)&&(!centredB)){
    				if(oppCount==0){ 
    					if(section==8){score = 9;} // creates a three piece and off centred 1 //easily blocked
    					else{score = 8;}}
        			else{ score = 7;} // just a 3 piece (this the time to use some prob)
   				}
    			break;
    		case 2: //usually 2 on the board
    			if((!unbalanced)&&((centredA&&!centredB)||(!centredA&&centredB))){ //will build a 2 and a centred 1
    				if(oppCount==0){score = 9;}
    				else{ score = 7;}
    			}	
    			else if((!moveQuadBigger)&&unbalanced&&(centredA||centredB)){ //will build a 2 and a centred 1
    				if(oppCount==0){ score = 8;}
    				else{ score =7;}
    			}
    			else if(centredA&&centredB&&(!unbalanced)){
    				if(oppCount==0){ score = 8;} // good strong move will build a 2 and a centred 1
    				else{ score = 7;}
    			}
    			else if(((unbalanced)&&(moveQuadBigger))&&((!centredA)||(!centredB))){ //will build a three section
    				if(oppCount==0){ 
    	    			if(goalState){
    	    				score = 9;}
    	    			else{
    					score = 6;}}
    				else{ score = 5;}
    			}
    			else if((!unbalanced)&&(!centredA)&&(!centredB)){ // two off centred in diff quads build a 2 and off centred 1
    				if(oppCount==0){ score = 3;}
    				else{ score = 2;}
    			}
    			break;
    		case 1: //when 1 on the board
    			if((centredA||centredB)&&(!moveQuadBigger)){
    				if(oppCount==0){ score = 4;} // not that bad move will be in smaller quad
    				else{ score = 3;}
    			}
    			else{
    				if(oppCount==0){ score = 2;} //move will be beside it bad move as no partner highly blocakble
    				else{ score = 0;}
    			}
    			break;	
    		default: //none on the board
    			if(oppCount==0){ score = 2;} 
				else{ score = 0;}
    	}
    	if((section == 2 || section == 3) && (score!=0||section!=8)){
    		score--;
    	}
    		return score;
    }
    
    
    public static HashMap<Integer,ArrayList<MoveData>> horzDiagVertMoveFinder(String playerPiece, PentagoBoardState boardState, boolean defence, boolean maximizingPlayer){
    	
    	HashMap<Integer,ArrayList<MoveData>> solutionsMap = new HashMap<Integer,ArrayList<MoveData>>();
    	MoveData result;
    	int section = 0;
    	int k=0;
    	while(k<3){
    		int[][] quadCoords = new int[2][2];
			for(int i=0;i<3;i++){
				switch(i){
					case 0: 
						section = 1;
						break;
					case 1: 
						section = 0;
						break;
					case 2: 
						section = 2;
						break;
				}
		    	for(int qA=0; qA<3;qA++){ //searches left 2 then right 2 top 1st always
		    		for(int qB=qA+1;qB<4;qB++){//searches left 2 then right 2
		    			if(k==0){
		    				quadCoords[0] = quadToCoord(section,1,qA);// X is fixed so its traversing rows so a horizontal search 
		    				quadCoords[1] = quadToCoord(section,1,qB);
		    				result = horizVertDiagSearch(quadCoords,k, boardState, playerPiece, defence,maximizingPlayer); // if def true it does strat 8 scoring
		    			}
		    			else if(k==1){
		    				quadCoords[0] = quadToCoord(1,section,qA);// Y is fixed so its traversing columns so a vertical search 
		    				quadCoords[1] = quadToCoord(1,section,qB);
		    				result = horizVertDiagSearch(quadCoords,k, boardState, playerPiece, defence,maximizingPlayer);
		    			}
		    			else{
		    				if(section == 2){
		    					break;
		    				}
		    				quadCoords[0] = quadToCoord(1,1,qA);// Y is fixed so its traversing columns so a vertical search 
		    				quadCoords[1] = quadToCoord(1,1,qB);
		    				if(section==0){
			    				result =horizVertDiagSearch(quadCoords, 2,  boardState, playerPiece, defence,maximizingPlayer);
		    				}
		    				else{
			    				result = horizVertDiagSearch(quadCoords, 3,  boardState,playerPiece, defence,maximizingPlayer);
		    				}
		    			}
		    			if(result==null){
		    				continue;
		    			}
		    			else if((result.getCoords()[0]==-1)||(result.getCoords()[0]==-1)){
		    				continue;
		    			}
		    			else if(solutionsMap.containsKey((Integer)result.getScore())){
		    				ArrayList<MoveData> arr = solutionsMap.get((Integer)result.getScore());
		    				arr.add(result);
		    				solutionsMap.remove((Integer) result.getScore());
		    				solutionsMap.put((Integer) result.getScore(),arr);
		    			}
		    			else{
		    				ArrayList<MoveData> arr = new ArrayList<MoveData>();
		    				arr.add(result);
		    				solutionsMap.put((Integer) result.getScore(), arr); 
		    			}	
		    		}
		    	}
			}
			k++;
    	}
    	HashMap<Integer,ArrayList<MoveData>> solutionsMapUpdated = threeDiagTest(solutionsMap, boardState, playerPiece,maximizingPlayer);
    	return solutionsMapUpdated;
    }
    
    public static MoveData horizVertDiagSearch(int[][] quadCentres, int strategies,PentagoBoardState boardState, String playerPiece, boolean defence,boolean maximizingPlayer){ // optimize
    	String oppPiece = "";
    	if(playerPiece.equals("b")){
    		oppPiece = "w";
    	}
    	else if(playerPiece.equals("w")){
    		oppPiece = "b";
    	}
    	boolean centredA = (!boardState.isPlaceLegal(new PentagoCoord(quadCentres[0][0],quadCentres[0][1]))),centredB = (!boardState.isPlaceLegal(new PentagoCoord(quadCentres[1][0],quadCentres[1][1]))), unCentred = false, zeroZeroError = false;
    	int [][][] coord = new int[2][2][2];
    	int pieceCountA = 0, pieceCountB = 0,oppPieceCount = 0, score = 0, posA = 0, posB = 0,y = 0, x = 0;
    	if((!centredA)||(!centredB)){
    		unCentred = true;
    	}
    	for(int i= -1; i<2; i++){
    		if(strategies==0){
    			x = i;
    		}else if(strategies==1){
    			y = i;
    		}else if(strategies==2){ // diagonal from downright to up-left
    			y=i;
    			x=i;
    		}else if(strategies==3){ // diagonal from downleft to up-right
    			y=-i;
    			x=i;		
    		}
    		String PieceLocA = boardState.getPieceAt(quadCentres[0][0]+y,quadCentres[0][1]+x).toString();
    		String PieceLocB = boardState.getPieceAt(quadCentres[1][0]+y,quadCentres[1][1]+x).toString();
    		
    		if(PieceLocA.toString().equals(oppPiece)){
    		 		oppPieceCount++;
    		   		if((oppPieceCount>1)||(i==0)){
    		   			return null;
    		    	}
    		}
    		else if(boardState.isPlaceLegal(new PentagoCoord(quadCentres[0][0]+y,quadCentres[0][1]+x))&&(i!=0)){
    			if( (quadCentres[0][0]+y)==0 && (quadCentres[0][1]+x)==0){
    				 zeroZeroError =true;
	    		}
	    		coord[0][posA][0] = (quadCentres[0][0]+y); //Y
				coord[0][posA][1] = (quadCentres[0][1]+x); //X
				posA++;
    		}
	    	else if(i!=0){
	    		pieceCountA++;
	    	}
    		
    	   	if(PieceLocB.equals(oppPiece)){
    		   		oppPieceCount++;
		   			if((oppPieceCount>1)||(i==0)){
		    			return null;
			    	}
    		}
    	    else if(boardState.isPlaceLegal(new PentagoCoord(quadCentres[1][0]+y,quadCentres[1][1]+x))&&(i!=0)){
    	    			if( (quadCentres[1][0]+y)==0 && (quadCentres[1][1]+x)==0){
    	    				 zeroZeroError = true;
        	    		}
        	    		coord[1][posB][0] = (quadCentres[1][0]+y); //Y
        				coord[1][posB][1] = (quadCentres[1][1]+x); //X
    					posB++;
    	    }
    	   	else if(i!=0){
	    		pieceCountB++;
        	}	
    	}
    	if(defence){
    		strategies = 8; // effects scoring
    	}
    	if(unCentred){	
    		if((!centredA)&&(!centredB)){ // both have no centre
    			MoveData res;
		    	switch(pieceCountA){
		    		case 0: //int score, int oppCount, boolean goal,boolean centredA, boolean centredB, boolean moveQuadBigger, boolean unbalanced
		    			if(pieceCountB==2){ score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(1,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else{score = horzVertDiagScoreGuide(0,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[1][0],quadCentres[1][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res;
		    		case 1:
		    			if(pieceCountB==2){score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else{
			    			score = horzVertDiagScoreGuide(1,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);
							res = new MoveData(quadCentres[0][0],quadCentres[0][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
					    	return res;
				    	}
						res = new MoveData(quadCentres[1][0],quadCentres[1][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; // could add randomness
		    		case 2:
		    			if(pieceCountB==2){score = horzVertDiagScoreGuide(4,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else{score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[0][0],quadCentres[0][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 
		    		}
		    	}
		    else if(centredA){ // B is uncentred (A is centred so atleast on Piece in A but up to 2 neighbours possible)
		    	MoveData res; 
		    	switch(pieceCountA){ 
					case 0:
		    			if(pieceCountB==2){score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else{ score = horzVertDiagScoreGuide(1,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[1][0],quadCentres[1][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 
					case 1: // 2 on A (centred)
						if(pieceCountB==2){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else{
		    				if(boardState.getTurnNumber()<3&&(realBoardState.getTurnPlayer()==1)){score = horzVertDiagScoreGuide(2,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    				else{score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}}	
		    			res = new MoveData(quadCentres[1][0],quadCentres[1][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 
					case 2: // 3 on A
						if(pieceCountB==2){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else if(pieceCountB==1){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else{score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[1][0],quadCentres[1][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 	
		    		}
		    	}
		    else { // B is centred A isnt
		    	MoveData res;
		    	switch(pieceCountB){
					case 0:
						if(pieceCountA==2){ score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,true,true,strategies,maximizingPlayer);}
		    			else if(pieceCountA==1){score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else{ score = horzVertDiagScoreGuide(1,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[0][0],quadCentres[0][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res;
					case 1:
						if(pieceCountA==2){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,false,strategies,maximizingPlayer);}
		    			else if(pieceCountA==1){score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else{
		    				if(boardState.getTurnNumber()<3&&(realBoardState.getTurnPlayer()==1)){score = horzVertDiagScoreGuide(2,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    				else{score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}}
						res = new MoveData(quadCentres[0][0],quadCentres[0][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 
					case 2:
						if(pieceCountA==2){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else if(pieceCountA==1){score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);}
		    			else{score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);}
						res = new MoveData(quadCentres[0][0],quadCentres[0][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
		    			return res; 
		    		}
		    	}
    		}
    	else{ // both centred
    		if(pieceCountA==pieceCountB){ //best Move
    			for(int j=0; j<2;j++){
	    			for(int k=0; k<2;k++){
	    				if(boardState.isPlaceLegal(new PentagoCoord(coord[j][k][0],coord[j][k][1]))){
	    					//System.out.println("MyMoveE: { "+coord[j][k][0]+", "+coord[j][k][1]+"}");
	    					if((coord[j][k][0]==0) && (coord[j][k][1]==0)&&(!zeroZeroError)){
	    						 continue;
	    						 //quick work around for a logical error
	    					}
	    					if(pieceCountB==1){ 
	    						score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,false,strategies,maximizingPlayer);
								MoveData res = new MoveData(coord[j][k][0],coord[j][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
	    		    			return res; 
	    					}
	    					else if(pieceCountB==0){
	    						score = horzVertDiagScoreGuide(2,oppPieceCount,false,centredA,centredB,false,false,strategies,maximizingPlayer);
								MoveData res = new MoveData(coord[j][k][0],coord[j][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
	    		    			return res;
	    					}
	    					else{return null;} // 6 pieces and should have won by now
	    				}
	    			}	
	    		}
	    	}
	    	else if(pieceCountA<pieceCountB){ // if either is above 0 then worth blocking
	    		for(int k=0; k<2;k++){
					if(boardState.isPlaceLegal(new PentagoCoord(coord[0][k][0],coord[0][k][1]))){
						if((coord[0][k][0]==0) && (coord[0][k][1]==0)&&(!zeroZeroError)){
							 continue;
							 //quick work around for a logical error
						}
						if(pieceCountB==2){ // means three is 3 in one and atleast one in another
							score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);
							MoveData res = new MoveData(coord[0][k][0],coord[0][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
    		    			return res; 
						}
						else if(pieceCountB==1) {
							score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);
							MoveData res = new MoveData(coord[0][k][0],coord[0][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
    		    			return res; 
						}
					}
					k++;
				}
	    	}
	    	else if(pieceCountA>pieceCountB){
	    		for(int k=0; k<2;k++){
					if(boardState.isPlaceLegal(new PentagoCoord(coord[1][k][0],coord[1][k][1]))){
						if((coord[1][k][0]==0) && (coord[1][k][1]==0)&&(!zeroZeroError)){
							 continue;
							 //quick work around for a logical error
						}
						if(pieceCountA==2){
							score = horzVertDiagScoreGuide(4,oppPieceCount,true,centredA,centredB,false,true,strategies,maximizingPlayer);
							MoveData res = new MoveData(coord[1][k][0],coord[1][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
    		    			return res; 
						}
						else if((pieceCountA==1)){
							score = horzVertDiagScoreGuide(3,oppPieceCount,false,centredA,centredB,false,true,strategies,maximizingPlayer);
							MoveData res = new MoveData(coord[1][k][0],coord[1][k][1],coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategies,boardState);
    		    			return res; 
						}
					}
					k++;
				}
	    	}
    	}	
	    return null;
    }
    public static int threeDiagScoreGuide(int cornerOppCount, int pieceCount, boolean cornerPieceFound,boolean centredA,boolean centredB,int strategy, boolean maximizingPayer){
    	//can assumer anything that enters switch has open corners and no opp pieces in key locations
    	//cornerOppCount is always 1 or 0
    	int score = 0;
    	switch(pieceCount){
    		case 4:
    			score = 13; // have now won (assuming quads in place)
    			break;
    		case 3:
    			if(cornerPieceFound){ //3 on the board + corner (so 4)
    				score = 13;
    			}
    			else if(cornerOppCount==0){ // 1 move away from winning but gaurenteed win
    				score = 12;
    			}
    			else{
    				score = 10; // on defense block corner (1 block away from losing)
    			}
    			break;
    		case 2:
    			if(cornerPieceFound){ //3 on the board (1 block away from losing)
    				score = 10;
    			}
    			else if(centredA||centredB){
    				score = 8-(cornerOppCount*2); //7-8
    			}	
    			else{
    				score = 7-(cornerOppCount*2); //6-7
    			}	
    			break;
    		case 1:
    			if(cornerPieceFound){ //2 on the board
    				score = 5;
    			}
    			else{
    				score = 3-(cornerOppCount*2); //2-3
    			}	
    			break;
    		case 0:
    			if(cornerOppCount==0){
    				score = 1;
    			}
    			break;
    	}
    	if(strategy == 8&&score<12){
    		score--;
    	}	
    		return score;
    }

    
    public static HashMap<Integer,ArrayList<MoveData>> threeDiagTest(HashMap<Integer,ArrayList<MoveData>> solutionsMap, PentagoBoardState  boardState, String playerPiece, boolean maximizingPlayer){
     	//HashMap<Integer,ArrayList<Move>> solutionsMap = new HashMap<Integer,ArrayList<Move>>();
    	
     	int[][] centres = {{0,1},{2,1}};
     	int corners[][][] = {
     			{{2,0},{5,0},{2,3},{5,3}},//+1+1
     			{{2,2}, {2,5},{5,2},{5,5}},//+1,-1
     			{{0,0},{3,0},{0,3},{3,3}},//-1,+1
     			{{0,2},{3,2},{0,5},{3,5}}}; //-1,-1
    	int k=0;
    	while(k<4){
	    	int[][] quadCoords = new int[2][2];
			    for(int qA=0; qA<3;qA++){ //searches left 2 then right 2 top 1st always
			    	for(int qB=qA+1;qB<4;qB++){//searches left 2 then right 2
			    		if(k<2){
			    			quadCoords[0] = quadToCoord(centres[0][0],centres[0][1],qA);// X is fixed so its traversing rows so a horizontal search 
			    			quadCoords[1] = quadToCoord(centres[0][0],centres[0][1],qB);
			    		}
			    		else{
			    			quadCoords[0] = quadToCoord(centres[1][0],centres[1][1],qA);// X is fixed so its traversing rows so a horizontal search 
			    			quadCoords[1] = quadToCoord(centres[1][0],centres[1][1],qB);
			    		}
			    		MoveData result = threeQuadDiagonal(quadCoords,k,corners[k], boardState, playerPiece,maximizingPlayer);
			    		if(result==null){
			    			continue;
			    		}
			    		int[] coord = result.getCoords();
		    			if((coord[0]<0)||(coord[1]<0)){
		    				continue;
		    			}
			    		else if(solutionsMap.containsKey(result.getScore())){
			    			ArrayList<MoveData> arr = solutionsMap.get(result.getScore());
			    			arr.add(result);
			    			solutionsMap.remove(result.getScore());
			    			solutionsMap.put(result.getScore(),arr);
			    		}
			    		else{
			    			ArrayList<MoveData> arr = new ArrayList<MoveData>();
			    			arr.add(result);
			    			solutionsMap.put(result.getScore(),arr); 
			    		}	
			    	}
		    	}
				k++;
	    	}	
    	return solutionsMap;
    }

    
    public static MoveData threeQuadDiagonal(int[][] quadCentres, int section, int[][] cornerCoords,PentagoBoardState boardState,String playerPiece, boolean maximizingPlayer){ // still does illegel moves trying to access(0,0) corner error
    	int x=1,y=1,pieceCount=0;
    	int strategy = 0;
    	String oppPiece = "";
    	if(section>=2){
    		y=-1;
    		strategy = 4; // going up and right
    	}
    	else{
    		strategy = 5;  // going down and left
    	}
    	if(section%2==1){
    		x=-1;
    		strategy = 4;  // going up and right
    	}
    	else{
    		strategy = 5; // going down and left
    	}
    	if(playerPiece.equals("b")){
    		oppPiece = "w";
    	}
    	else if(playerPiece.equals("w")){
    		oppPiece = "b";
    	}
    	
    	int quadA = coordToQuad(quadCentres[0][0],quadCentres[0][1]),quadB = coordToQuad(quadCentres[1][0],(quadCentres[1][1]));
       	int[][] pieces = {{quadCentres[0][0],(quadCentres[0][1])},{quadCentres[1][0],(quadCentres[1][1])},{quadCentres[0][0]+y,(quadCentres[0][1]+x)},{quadCentres[1][0]+y,(quadCentres[1][1]+x)}};
		int[][] corner = new int[2][2];
		
		int oppPieceCount = 0, cornerSpaceCount = 0;
		boolean playerPieceFoundInCorner = false;  // find a player piece in the corner
		boolean centredA = (boardState.getPieceAt(pieces[0][0]+y, pieces[0][1]).toString().equals(playerPiece)), centredB = (boardState.getPieceAt(pieces[1][0]+y, pieces[1][1]).toString().equals(playerPiece));

		for(int i=0; i<4; i++){
			if(boardState.getPieceAt(pieces[i][0], pieces[i][1]).toString().equals(oppPiece)){
				return null;
			}
			else if(boardState.isPlaceLegal(new PentagoCoord(pieces[i][0], pieces[i][1]))){ // legal place
				y = pieces[i][0]; // takes the 1st legal place found
				x = pieces[i][1];
				//nonCornerSpaceFound = true;
			}
			else{ // means a playerPiece found so we increment pieceCount
				pieceCount++;
			}
			if(((quadA!=coordToQuad(cornerCoords[i][0],cornerCoords[i][1]))&&(quadB!=coordToQuad(cornerCoords[i][0],cornerCoords[i][1])))){ //ensures its on a seperate quad	
				if(boardState.isPlaceLegal(new PentagoCoord(cornerCoords[i][0],cornerCoords[i][1]))){
					corner[cornerSpaceCount][0]= cornerCoords[i][0];// takes the 1st legal corner found
					corner[cornerSpaceCount][1]= cornerCoords[i][1];
					cornerSpaceCount++;
				}
				else if(boardState.getPieceAt(cornerCoords[i][0], cornerCoords[i][1]).toString().equals(oppPiece)){
					oppPieceCount++;
					if(oppPieceCount>1){
						return null;
					}
				}
				else{// means a playerPiece found so we increment pieceCount
					playerPieceFoundInCorner = true;
				}
			}
		}	
		if((cornerSpaceCount==0)&&(!playerPieceFoundInCorner)){
			return null;
		}
		
		if((pieceCount<4)){
			int score = threeDiagScoreGuide(oppPieceCount,pieceCount,playerPieceFoundInCorner, centredA, centredB, strategy,maximizingPlayer);
			MoveData res = new MoveData(y,x,coordToQuad(quadCentres[0][0],quadCentres[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategy,boardState);
			return res;
		}
		else if(pieceCount>=4){
			strategy +=2;
			int score = threeDiagScoreGuide(oppPieceCount,pieceCount,playerPieceFoundInCorner,centredA, centredB, strategy,maximizingPlayer);
			MoveData res = new MoveData(corner[0][0],corner[0][1],coordToQuad(corner[0][0],corner[0][1]),coordToQuad(quadCentres[1][0],quadCentres[1][1]),score,strategy,boardState);
			return res;
		}
		return null;
    }
    
    public static Move createMove(MoveData move){
 
    	if(move.getScore()==13&&move.getStrategy()!=8){
    		goalState=true;
    	}
    	int[] coord = move.getCoords();
    	if(move.getScore()==-1){
    		Quadrant[] q = selectQuad(-1,-1);
    		return ((Move)new PentagoMove(coord[0],coord[1],q[0],q[1],move.getBoard().getTurnPlayer()));
    	}else{
    		int[] quads = quadMatchUp(move);
    		Quadrant[] q = selectQuad(quads[0],quads[1]);
    		return ((Move)new PentagoMove(coord[0],coord[1],q[0],q[1],move.getBoard().getTurnPlayer()));	
    	}
    }
    
    
    
    public static Quadrant[] selectQuad(int quadA, int quadB){ // NOTE: if value is NOT 0, 1, 2, or 3  (i.e -1) IT selects a random quadrant
    	if(quadA > 3 || quadA < 0){
    		quadA = (int)(Math.random() * (4 - 0 ));
    	}
    	if(quadB > 3 || quadB < 0){
    		quadB = (int)(Math.random() * (4 - 0 ));
    	}
    	if(quadA == quadB){
    		return selectQuad(-1,-1);
    	}
    	int temp[] = {quadA,quadB};
    	Quadrant[] q = new Quadrant[2];
    
    	for(int i=0; i<2;i++){
	    	switch(temp[i]){
	    	  case 0:
	    		   q[i] = Quadrant.TL;
	    		   break;
	    	  case 1:
	    		   q[i] = Quadrant.TR;
	    		   break;
	    	  case 2:
	    		  q[i] = Quadrant.BL;
	    		  break;
	    	  case 3:
	    		  q[i] = Quadrant.BR;
	    		  break;
	      	  default:
	      		  q[i] = Quadrant.TL;
	      		  break;
	    	}
    	}
    	return q;
    }
    public static int coordToQuad(int y, int x){
    	if((y<6 && y>-1 && x<6 && x>-1)){
    		if(y>2){ //bottom half (3 or 4)
    			if(x<3){ //BL
    				return 2;
    			}
    			else{//BR
    				return 3; 
    			}
    		}
    		else{
    			if(x<3){ //TL
    				return 0;
    			}
    			else{//TR
    				return 1; 
    			}
    		}
    	}
    	return -1;
    }
    
    public static int[] quadToCoord(int y, int x, int quad){
    	if(!(y>=3||y<0||x>=3||x<0||quad>=4||quad<0)){
	    	int[] arr = new int[2];
	    	switch(quad){
	    		case 0: //TL
	    			arr[0] = y;
	    			arr[1] = x;
	    			return arr;
	    		case 1: //TR
	    			arr[0] = y;
	    			arr[1] = x+3;
	    			return arr;
	    		case 2: //BL
	    			arr[0] = y+3;
	    			arr[1] = x;
	    			return arr;
	    		case 3: //BR
	    			arr[0] = y+3;
	    			arr[1] = x+3;
	    			return arr;
	    	}
    	}	
    	return null;
    }
    

  public static int[] quadMatchUp(MoveData move){
	  int[] coord = move.getCoords();
	  int q = coordToQuad(coord[0],coord[1]), approxQA = -1, approxQB = -1;
	  int qA = move.getqA(), qB = move.getqB();
	
	 if((move.getStrategy()==8)&&(!goalState)){
		 int[] res = {approxQA,approxQB};
		 return res;
	 }
	 if((testApproximateMatch(qA, qB, move.getStrategy(), move.getBoard())==move.getStrategy())
			 &&(!goalState)){ // already in the correct orientation
		 int[] res = new int[2];
		 int k=0;
		 for(int i=0; i<4;i++){
			 if(i!=qA&&i!=qB){
				 res[k]=i;
				 k++;
			 }
		 }
	  }
	  boolean matchFound = false;
	  PentagoBoardState safeState =  (PentagoBoardState) move.getBoard().clone();
	  for(int i=0; i<3;i++){ //searches every quad switch combo
		   	for(int j=i+1;j<4;j++){
		   		if(!((i == qA || j == qB)||(j == qA || i == qB))){
		   			continue;
		   		}
		   		try{
		   			//((Move)new PentagoMove(coord[0],coord[1],i,j,safeState.getTurnPlayer()));	
		   			Quadrant[] quadsSelect = selectQuad(i,j);
		   			safeState.processMove((PentagoMove) new PentagoMove(coord[0],coord[1],quadsSelect[0],quadsSelect[1],safeState.getTurnPlayer()));
		   		}catch(Exception e){
		   			continue;
		   		}
		   	if(goalState){
				if(safeState.getWinner()==(1-safeState.getTurnPlayer())){
	  				qA = i;
	  				qB = j;
	  				matchFound = true;
			  		break;
			  	}
			}
			else{
				int tempA = 0, tempB = 0;
				if((i == qA && j == qB)||(j == qA && i == qB)){
					tempA = qB;
					tempB = qA;
				}
				else if(i==qA){
					tempA = i;
				}
				else if(i==qB){
					tempB = i;
				}
				else if(j==qA){
					tempA = j;
				}	
				else if(j==qB){
					tempB = j;
				}	
				if(testApproximateMatch(tempA, tempB, move.getStrategy(), safeState)==move.getStrategy()){
					approxQA = i;
		  			approxQB = j;
		  			break;
		  		}
			}
			safeState =  (PentagoBoardState) move.getBoard().clone();
	   	 }	
	}	
	if(matchFound){
		 int[] res = {qA,qB};
		 return res;
	}
	else{
		 int[] res = {approxQA,approxQB};
		 return res;
	}
	  
  }
  
 public static int testApproximateMatch(int qA, int qB, int strategy, PentagoBoardState state){
	 //qA and qB after simulated swap (new qA/qB)
	int quadOrientation = -1;
	 if(qA==0||qA==2){ 
			if(qB==(qA+1)){ // horizontal
				quadOrientation = 0;
			}
			else if((qA==0&&qB==(qA+2))||
					(qA==2&&qB==(qA-2))){ //vertical
				quadOrientation = 1;
			}else{ //diagonal
				if(qA==0&&qB==(qA+3)){ //(0,3)
					if(strategy <4){
						quadOrientation = 2;
					}
					else{
						quadOrientation = 4;
					}
				}
				else if(qA==2&&qB==(qA-1)){ //(1,2)
					if(strategy <4){
						quadOrientation = 3;
					}
					else{
						quadOrientation = 6;
					}
				}
			}
		}
		else if(qA==1||qA==3){
			if(qB==(qA-1)){ // horizontal
				quadOrientation = 0;
			}
			else if((qA==1&&qB==(qA+2))||
					(qA==3&&qB==(qA-2))){ //vertical
				quadOrientation = 1;
				
			}else{ //diagonal
				if(qA==1&&qB==(qA+1)){ //(1,2)
					if(strategy <4){
						quadOrientation = 2;
					}
					else if(strategy <6){
						quadOrientation = 4;
					}
					else if(strategy == 7){
						quadOrientation = 7;
					}
				}
				else if(qA==3&&qB==(qA-3)){ //(0,3)
					if(strategy <4){
						quadOrientation = 3;
					}
					else if(strategy <6){
						quadOrientation = 5;
					}
					else if(strategy == 6){
						quadOrientation = 6;
					}
				}
			}
		}
	 return quadOrientation;
 	}
 
}