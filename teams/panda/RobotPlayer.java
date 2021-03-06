package panda;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path;
	static int bigBoxSize = 5;
	
	static int countNumRobots = 0;
	
	public static double[][] cowGrowth; //hamster

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
	static final int smallMapBuilder = 5;
	
	static int mapHerderConst = 0;
	static int mapBuilderConst = 0;
	
	

	public static int mapHeight;
	public static int mapWidth;
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		randall.setSeed(rc.getRobot().getID());

		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		
		System.out.println(mapHeight+" "+mapWidth+" "+((mapHeight + mapWidth)/2));
		
		cowGrowth = rc.senseCowGrowth();
				
		for (double[] row : cowGrowth) {
	        //for each number in the row
	        for (double j : row) {
	            System.out.print(j + " ");
	        }
	        System.out.println("");
	    }
		
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
			
		System.out.println(mapHerderConst+ " "+mapBuilderConst);	
		countNumRobots = rc.senseRobotCount();
		if(countNumRobots > mapHerderConst){ //if there are more than 10 robots on the field, build pastrs
			herder = true;
			System.out.println("herder");
		}
		else if(countNumRobots > mapBuilderConst){
			buildOrNot = true;
			System.out.println("builder");
		}
		else{
			attacker = true;
			System.out.println("attacker");
		}
		
		if(rc.getType()==RobotType.HQ){
			tryToSpawn();
		}else{
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
			try{
				if(rc.getType()==RobotType.HQ){ 
					runHQ();
				}else if(rc.getType()==RobotType.SOLDIER){
					runSoldier();
				}
			}catch (Exception e){
				//e.printStackTrace();
			}
			rc.yield();
		}
	}

	private static void runHQ() throws GameActionException {
		//tell robots where to go
		tryToSpawn();
	}

	public static void tryToSpawn() throws GameActionException {
		if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
	
	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		
		
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
		}else{
			
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
						//System.out.println("panda is building pasture!");
						rc.construct(RobotType.PASTR);
					}
				}
			}


			//follow breadthFirst path
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
//		return (Robot[])robots.toArray();
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
	
	private static int distanceTo(MapLocation l1, MapLocation l2){
		return (int) (Math.pow(l1.x - l2.x, 2) + Math.pow(l1.y - l2.y, 2));
	}
	
}