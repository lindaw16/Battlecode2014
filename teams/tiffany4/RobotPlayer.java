package tiffany4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path;
	static int bigBoxSize = 5;
	
	static int countNumRobots = 0;
	
	public static double[][] cowGrowth; //hamster
	public static double [][] newCowGrowth;
	static int noise_count = 0;
	static int pastr_count = 0; 
	public static MapLocation newPastrLoc; 
	
	static boolean pastrHQ = false;
	static boolean noiseTower = false;
	static boolean herder = false;
	static boolean attacker = false;
	static boolean buildOrNot = false;
	
	static MapLocation pastrGoal = null;
	static MapLocation enemyPastures[];
	static MapLocation myPastures[];
	
	static final int bigMapHerder = 14;
	static final int bigMapBuilder = 10;
	
	static final int medMapHerder = 12;
	static final int medMapBuilder = 8;
	
	static final int smallMapHerder = 9;
	static final int smallMapBuilder = 7;
	
	static int mapHerderConst = 0;
	static int mapBuilderConst = 0;
	

	static MapLocation mPoint; 
	
	static MapLocation pointsNoisetower[] = {};
	static int towerCount = 0;
	static int noiseConst = 24;
	
	public static int mapHeight;
	public static int mapWidth;
	
	public static boolean firstRobot = false;
	
	/*
	 * Message passing codes:
	 * 1 - attack this PASTR
	 * 1 + xCoord + 0 + yCoord
	 * 
	 * 
	 */
	
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		randall.setSeed(rc.getRobot().getID());
		mPoint = rc.getLocation();
		cowGrowth = rc.senseCowGrowth();
		
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		newCowGrowth = new double[mapWidth][mapHeight];
		
		if((mapHeight + mapWidth) / 2 < 40){
			mapHerderConst = smallMapHerder;
			mapBuilderConst = smallMapBuilder;
		}
		else if((mapHeight + mapWidth) / 2 < 60){
			mapHerderConst = medMapHerder;
			mapBuilderConst = medMapBuilder;
		}
		else{
			mapHerderConst = bigMapHerder;
			mapBuilderConst = bigMapBuilder;
		}
		
		mapHerderConst += 2;
		mapBuilderConst += 2;
		
		countNumRobots = rc.senseRobotCount();
		
		if(countNumRobots > mapHerderConst){ //if there are more than 10 robots on the field, build pastrs
			herder = true;
			System.out.println("herder");
		}
		else if(countNumRobots > mapBuilderConst){
			buildOrNot = true;
			System.out.println("builder");
		}
		else if(countNumRobots == 2){
			pastrHQ = true;
			System.out.println("pastrHQ");
		}
		else if(countNumRobots == 1){
			noiseTower = true;
			firstRobot = true;
			System.out.println("noisetower");
			
		}
		else{
			attacker = true;
			System.out.println("attacker");
		}
		
		if(rc.getType()==RobotType.HQ){
			
		}
		else if(rc.getType()==RobotType.NOISETOWER){
			System.out.println("PASTR GOALLLLL!");
			recalculateFirst();
			System.out.println("PASTR GOALLLLL!");
		//	locatePastr(cowGrowth);
			//System.out.println("PASTR GOAL!: "+newPastrLoc.x+" "+newPastrLoc.y);
			System.out.println("noiseTower");
		}
		else if(rc.getType()==RobotType.SOLDIER){
			BreadthFirst.init(rc, bigBoxSize);
			MapLocation goal = getRandomLocation();
			path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
			//VectorFunctions.printPath(path,bigBoxSize);
		}
		
		
		
		//generate a coarsened map of the world
		//TODO only HQ should do this. The others should download it.
//		MapAssessment.assessMap(4);
//		MapAssessment.printBigCoarseMap();
//		MapAssessment.printCoarseMap();

		while(true){
			if(rc.isActive()){
				try{
					if(rc.getType()==RobotType.HQ){
						runHQ();
					}else if(rc.getType()==RobotType.SOLDIER){
						runSoldier();
					}
					else if(rc.getType()==RobotType.NOISETOWER){
						runNoisetower();
					}
				}catch (Exception e){
					System.out.println(e);
					
				}
			}
			rc.yield();
		}
	}

	private static void runHQ() throws GameActionException {
		//tell robots where to go
		if(rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}

	private static void runNoisetower() throws GameActionException {
		noiseConst--;
		
		if(noiseConst <= 3){
			noiseConst = 24;
			towerCount++;
		}
		towerCount %= 8;
		recalculateOne(towerCount);
		System.out.println(noiseConst);
		rc.attackSquare(pointsNoisetower[towerCount]);
	}
	
	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		
//		if(pastrHQ){
//			rc.construct(RobotType.PASTR);
//		}
//		else if(noiseTower){
//			rc.construct(RobotType.NOISETOWER);
//		}
//		
		if(rc.getHealth()<10){
			System.out.println("self-destruct!");
			rc.selfDestruct();
		}
		
		if(enemyRobots.length>0){//if there are enemies
			rc.setIndicatorString(0, "There are enemies");
			MapLocation[] robotLocations = new MapLocation[enemyRobots.length];
			for(int i=0;i<enemyRobots.length;i++){
				Robot anEnemy = enemyRobots[i];
				RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
				robotLocations[i] = anEnemyInfo.location;
			}
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
			if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				rc.setIndicatorString(1, "trying to shoot");
				if(rc.isActive()){
					rc.attackSquare(closestEnemyLoc);
				}
			}else{
				rc.setIndicatorString(1, "trying to go closer");
				Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
				simpleMove(towardClosest);
			}
		}
		
		
		
		else{
			
			if(attacker) {
				boolean alreadySense = false;
				if(pastrGoal != null && distanceTo(rc.getLocation(), pastrGoal) <= 10){
					enemyPastures = rc.sensePastrLocations(rc.getTeam().opponent());
					if(!Arrays.asList(enemyPastures).contains(pastrGoal)){
						pastrGoal = null;
						alreadySense = true;
					}
				}
				if(pastrGoal == null) {
					if(!alreadySense){
						enemyPastures = rc.sensePastrLocations(rc.getTeam().opponent());
					}
					pastrGoal = findClosest(enemyPastures, rc.getLocation());
				}
				
				if(path.size() == 0){
					path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(pastrGoal,bigBoxSize), 100000);
				}
				buildOrNot = false;

			}
			else if(herder){
				if(pastrGoal == null) {
					myPastures = rc.sensePastrLocations(rc.getTeam());
					if(myPastures.length < 3){
						buildOrNot = true;
					}
					pastrGoal = findClosest(myPastures, rc.getLocation());
				}
				if(path.size() == 0){
					path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(pastrGoal,bigBoxSize), 100000);
				}
			}
			else if(pastrHQ || noiseTower){
				path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(newPastrLoc,bigBoxSize), 100000);
				System.out.println("Trying to find ideal pastr loc"); 
			}
			if(buildOrNot) //no enemies, build a pasture
			  {
				Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 10);
				boolean isPASTR = false;
				for (Robot r: nearbyRobots){
					RobotInfo rInfo;
					rInfo = rc.senseRobotInfo(r);
					if(rInfo.type == RobotType.PASTR){
						isPASTR = true;
						break;
					}	
				}
				if (!isPASTR){
					if (Math.random() < 0.01 && rc.isActive())
					{
						//System.out.println("tiffany2 is building pasture!");
						rc.construct(RobotType.PASTR);
					}
				}
			}


			//follow breadthFirst path
			if (pastrHQ || noiseTower) {
				if(pastrHQ){
					rc.construct(RobotType.PASTR);
				}
				else if(noiseTower){
					rc.construct(RobotType.NOISETOWER);
				}
			}
			Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
			BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
		}
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//simpleMove(towardEnemy);
		
	}
	
	private static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		for(int directionalOffset:directionalLooks){
			int forwardInt = chosenDirection.ordinal();
			Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
			if(rc.canMove(trialDir)){
				rc.move(trialDir);
				break;
			}
		}
	}
	
	
//	private static Robot[] filter(Robot list[], RobotType type){
//		ArrayList<Robot> robots = new ArrayList<Robot>();
//		for(Robot r: list){
//			if(!rc.senseRobotInfo(r).type.equals(type)){
//				robots.add(r);
//			}
//		}
//		return robots.toArray(Robot[0]);
//	}
//	
	
	
	
	private static MapLocation findClosest(MapLocation list[], MapLocation location) {
		int closeDist = Integer.MAX_VALUE;
		MapLocation closeLoc = null;
		for(MapLocation l: list){
			int dist = (int) (Math.pow(l.x - location.x, 2) + Math.pow(l.y - location.y, 2));
			if(dist < closeDist){
				closeDist = dist;
				closeLoc = l;
			}
		}
		return closeLoc;
		
	}
	
	private static void printLocs(MapLocation [] points){
		for(MapLocation p: points){
			System.out.print("("+p.x+","+p.y+") ");
		}
		System.out.println();
	}
	
	private static void recalculateFirst(){
		
		pointsNoisetower = new MapLocation[8];
	
		for(int i=0; i<8; i++){
			double constant = Math.pow((double)Math.abs(allDirections[i].dx) + (double)Math.abs(allDirections[i].dy), .5);
			
			int xVal = allDirections[i].dx;
			int yVal = allDirections[i].dy;
			int xValue = Math.max(0, (int)(mPoint.x + xVal*noiseConst / constant));
			int yValue = Math.max(0, (int)(mPoint.y + yVal*noiseConst / constant));
			xValue = Math.min(xValue, mapWidth);
			yValue = Math.min(yValue, mapHeight);
			pointsNoisetower[i] = new MapLocation(xValue, yValue);
		}
	}
	
	
	
	private static void recalculateOne(int index){
		double constant = Math.pow((double)Math.abs(allDirections[index].dx) + (double)Math.abs(allDirections[index].dy), .5);

		int xVal = allDirections[index].dx;
		int yVal = allDirections[index].dy;
		int xValue =  (int)(mPoint.x + xVal*noiseConst / constant);
		int yValue = (int)(mPoint.y + yVal*noiseConst / constant);
//		int xValue = Math.max(0, (int)(mPoint.x + xVal*noiseConst / constant));
//		int yValue = Math.max(0, (int)(mPoint.y + yVal*noiseConst / constant));
//		xValue = Math.min(xValue, mapWidth);
//		yValue = Math.min(yValue, mapHeight);

		while(xValue < 0 || yValue<0 || xValue >= mapWidth || yValue >= mapHeight){
			noiseConst--;
			xValue =  (int)(mPoint.x + xVal*noiseConst / constant);
			yValue = (int)(mPoint.y + yVal*noiseConst / constant);
		}
		System.out.println(xValue+" "+yValue);
		pointsNoisetower[index] = new MapLocation(xValue, yValue);

	}
	
	
	private static MapLocation[] filterPoints(MapLocation[] points, MapLocation loc, int range){
		ArrayList<MapLocation> newPoints = new ArrayList<MapLocation>();
		for(MapLocation p: points){
			if(distanceTo(p, loc) > range){
				newPoints.add(p);
			}
		}
		return newPoints.toArray(new MapLocation[0]);
	}
	
	private static int distanceTo(MapLocation l1, MapLocation l2){
		return (int) (Math.pow(l1.x - l2.x, 2) + Math.pow(l1.y - l2.y, 2));
	}


	public static void locatePastr(double [][] cowGrowth){
		for (int i=0; i < mapHeight +1 ; i++){
			for (int j=0; j < mapHeight + 1; i++){
				if (i == 0 && j!=0) {
				newCowGrowth[i][j] = newCowGrowth[i-1][j] + cowGrowth[i][j];	
				}
				else if (j == 0 && i!=0) {
				newCowGrowth[i][j] = newCowGrowth[i][j-1] + cowGrowth[i][j];	
				}
				else if (i == 0 && j ==0) {
				newCowGrowth[i][j] = cowGrowth[i][j];	
				}
				else{
				newCowGrowth[i][j] = cowGrowth[i][j] + newCowGrowth[i-1][j] + newCowGrowth[i][j-1] - newCowGrowth[i][j];
				}
			}				
		}
		double a = 0;
		double b = 0;
		int m = 24; //noisetower radius of attack
		double max_sum = 0;
		for (int i=0; i < mapHeight +1 -m ; i++){
			for (int j=0; j < mapHeight + 1 -m; i++){
			double best_submatrix_sum = newCowGrowth[i+m][j+m] - newCowGrowth[i][j+m] - newCowGrowth[i+m][j] + newCowGrowth[i][j];
				if (best_submatrix_sum > max_sum){
					max_sum = best_submatrix_sum;
					a = i/2.0;
					b = j/2.0;
					newPastrLoc = new MapLocation((int) a, (int) b); 
				}
		//signal for new noisetower/pastr to be built at/near i-m/2, j-m/2 		
			}
		} 
	}
}