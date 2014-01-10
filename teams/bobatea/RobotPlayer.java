package bobatea;

import java.util.ArrayList;
import java.util.Random;

import coarsenserPlayer.BreadthFirst;
import coarsenserPlayer.VectorFunctions;
import battlecode.common.*;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path;
	static int bigBoxSize = 5;
	public static int mapHeight;
	public static int mapWidth;
	public static int distPASTR = (mapHeight + mapWidth) / 2 / 20 + 3;;
	public static int numPASTR = 7;
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		randall.setSeed(rc.getRobot().getID());
		
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
		
		Robot[] teamRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(rc.getTeam().opponent());
		
		//first see if there is are enemy PASTRs
		
		if (enemyPASTRs.length > 0) //there are enemy PASTRs to plunder!
		{
			MapLocation closestEnemyPASTRloc = VectorFunctions.findClosest(enemyPASTRs, rc.getLocation());
			path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(closestEnemyPASTRloc,bigBoxSize), 100000);
			while (rc.getLocation().distanceSquaredTo(closestEnemyPASTRloc) > 2)
			{ //move closer to the pastrue
				Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
				rc.move(bdir);
			}
			rc.selfDestruct();
			System.out.println("robot used self destruct!!!!!");
		} else{
			//first build numPASTR of PASTRs
			int totalPASTRs = 0;
			for (Robot rTeam: teamRobots){
				if (rc.senseRobotInfo(rTeam).type == RobotType.PASTR)
				{
					totalPASTRs++;
				}
			}
			
			if (totalPASTRs < numPASTR)
			{
				Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, distPASTR);
				boolean isPASTR = false;
				for (Robot rOpp: nearbyRobots){
					RobotInfo rInfo;
					rInfo = rc.senseRobotInfo(rOpp);
					if(rInfo.type == RobotType.HQ){
						if (rInfo.type == RobotType.PASTR){
							isPASTR = true;
						}
						break;
					}	
				}
				if (!isPASTR){
					if (Math.random() < 0.01 && rc.isActive())
					{
						System.out.println("kebob is building pasture ");
						System.out.println(totalPASTRs);
						rc.construct(RobotType.PASTR);
					}
				}
			}
			//ENOUGH PASTRS
			
			//follow orders from HQ
			//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST
	
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
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
	
				if(path.size()==0){
					MapLocation goal = getRandomLocation();
					path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(rc.senseEnemyHQLocation(),bigBoxSize), 100000);
				}
				//follow breadthFirst path
				Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
				BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
			 }
			//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			//simpleMove(towardEnemy);
		}
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
	
}