package cowbob;

import cowbob.BasicPathing;
import cowbob.BreadthFirst;
import cowbob.Comms;
import cowbob.VectorFunctions;

import java.util.Random;

import cowbob.BandData;
//import cowbob.VectorFunctions;
import battlecode.common.*;

import java.util.ArrayList;

public class RobotPlayer
{
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	
	//EUNICE AND LINDA TRYING TO COMMUNICATE IN JAVA
	static MapLocation rallyPoint;
	static MapLocation targetedPastr;
	static BandData band1;
	static BandData band2;
	
	static MapLocation enemyHQ;
	
	
	public static ArrayList<Integer> squad1 = new ArrayList<Integer>();
	public static ArrayList<Integer> squad2 = new ArrayList<Integer>();
	static int bigBoxSize = 5;
	static int robCt;
	static int squad;
	static boolean isLeader;
	static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	static int myBand = 100;
	static int pathCreatedRound = -1;
	
	public static void run(RobotController rcIn)
	{
		rc = rcIn;
		randall.setSeed(rc.getRobot().getID());
		enemyHQ = rc.senseEnemyHQLocation();
		
		
		if (rc.getType() == RobotType.HQ)
		{
			band1 = new BandData(2, 0, 30, 20);
			band2 = new BandData(1, 0, rc.senseEnemyHQLocation().x, rc.senseEnemyHQLocation().y);		
		}
		else if (rc.getType() == RobotType.SOLDIER)
		{
			robCt = rc.senseRobotCount();
			int temp = robCt/5+1;
			if(temp%2 == 0){
				squad = 1;
			} else
			{
				squad = 2;
			}
			
			if(robCt%5 == 1){
				isLeader = true;
			}
		}
		
		
		
		while(true){
			if(rc.isActive()){
				try{
					if(rc.getType()==RobotType.HQ){
						runHQ();
					}else if(rc.getType()==RobotType.SOLDIER){
						runSoldier();
					}
//					else if(rc.getType()==RobotType.NOISETOWER){
//						runNoisetower();
//					}
				}catch (Exception e){
					System.out.println(e);	
					//e.printStackTrace();
				}
			}
			rc.yield();
		}
	}
	
	private static void runHQ() throws GameActionException {
		//tell robots where to go
		//DO THINGS HERE

		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000000,rc.getTeam());

		
		if(Clock.getRoundNum()>400&&alliedRobots.length<5){//call a retreat
			
			MapLocation startPoint = findAverageAllyLocation(alliedRobots);
			
			System.out.println("spawning "+rc.senseHQLocation());
			Comms.findPathAndBroadcast(2,startPoint,rc.senseHQLocation(),bigBoxSize,2);
			
			rallyPoint = rc.senseHQLocation();
		}else{//not retreating
			//tell them to go to the rally point
			
			Comms.findPathAndBroadcast(1,rc.getLocation(),rallyPoint,bigBoxSize,2);

			//if the enemy builds a pastr, tell sqaud 2 to go there.
			MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastrs.length>0){
				MapLocation startPoint = findAverageAllyLocation(alliedRobots);
				targetedPastr = getNextTargetPastr(enemyPastrs,startPoint);
				//broadcast it
				Comms.findPathAndBroadcast(2,startPoint,targetedPastr,bigBoxSize,2);
			}
		
		
		
		
		}
		
		
		
		//System.out.println("I AM BROADCASTINGGGGGGGGG");
		band1 = new BandData(Clock.getRoundNum(), 0, 30, 20);
		rc.broadcast(1, band1.concatenate());
		band2 = new BandData(Clock.getRoundNum(), 0, rc.senseEnemyHQLocation().x, rc.senseEnemyHQLocation().y);
		rc.broadcast(2, band2.concatenate());
		
		int num = rc.senseRobotCount();
		int i = 0;
		if(num<GameConstants.MAX_ROBOTS){
			for(i=0;i<8;i++){
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
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000, rc.getTeam().opponent());
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());
//		if(rc.getHealth()<10){
//			System.out.println("self-destruct!");
//			rc.selfDestruct();
//		}
		//System.out.println("running soldier");
		if(enemyRobots.length>0){//if there are enemies
			
			//System.out.println("ERMAHGERD ENEMIES");
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
			}
		} else {
//Read Broadcast	
		//System.out.println("reading broadcast");

		navigateByPath(alliedRobots);
		
//		int message = rc.readBroadcast(squad);
//		System.out.println("I'm part of squad " + squad + "!!!");
//		
//	
//		int[] m = decode(message);
//		System.out.println(message+ "is the message, got it?");
//		Direction loc = rc.getLocation().directionTo(new MapLocation(m[2], m[3]));
//		System.out.println("I'M GOING TOWARDS " + m[2] + " " + m[3] + "!!!!!!!!!!!!!!!");
//		simpleMove(loc);	
//		
//		
//
//		rc.setIndicatorString(1, "trying to go closer");
//		Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
//		simpleMove(towardClosest);
		}
	
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
		
	
	private static int[] decode(int message)
	{
		int[] m = new int[4];
		int [] conversion = {100000, 10000, 100, 1};
		
		for (int i = 0; i < 4; i++)
		{
			m[i] = message/conversion[i];
			message -= m[i]*conversion[i];
			//System.out.println("WE MADE IT " + i + " " + m[i]);
		}
		
		return m;
	}
	
	
	
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
	
	private static MapLocation getNextTargetPastr(MapLocation[] enemyPastrs,MapLocation startPoint) {
		if(enemyPastrs.length==0)
			return null;
		if(targetedPastr!=null){//a targeted pastr already exists
			for(MapLocation m:enemyPastrs){//look for it among the sensed pastrs
				if(m.equals(targetedPastr)){
					return targetedPastr;
				}
			}
		}//if the targeted pastr has been destroyed, then get a new one
		return VectorFunctions.findClosest(enemyPastrs, startPoint);
	}

	
	private static MapLocation findAverageAllyLocation(Robot[] alliedRobots) throws GameActionException {
		//find average soldier location
		MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(alliedRobots, rc, true);
		MapLocation startPoint;
		if(alliedRobotLocations.length>0){
			startPoint = VectorFunctions.meanLocation(alliedRobotLocations);
			if(Clock.getRoundNum()%100==0)//update rally point from time to time
				rallyPoint=startPoint;
		}else{
			startPoint = rc.senseHQLocation();
		}
		return startPoint;
	}
	
	private static void navigateByPath(Robot[] alliedRobots) throws GameActionException{
		if(path.size()<=1){//
			//check if a new path is available
			int broadcastCreatedRound = rc.readBroadcast(myBand);
			if(pathCreatedRound<broadcastCreatedRound){//download new place to go
				pathCreatedRound = broadcastCreatedRound;
				path = Comms.downloadPath();
			}else{//just waiting around. Consider building a pastr
				considerBuildingPastr(alliedRobots);
			}
		}
		if(path.size()>0){
			//follow breadthFirst path...
			Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
			//...except if you are getting too far from your allies
			MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(alliedRobots, rc, true);
			if(alliedRobotLocations.length>0){
				MapLocation allyCenter = VectorFunctions.meanLocation(alliedRobotLocations);
				if(rc.getLocation().distanceSquaredTo(allyCenter)>16){
					bdir = rc.getLocation().directionTo(allyCenter);
				}
			}
			BasicPathing.tryToMove(bdir, true,true, false);
		}
	}
	
	private static void considerBuildingPastr(Robot[] alliedRobots) throws GameActionException {
		if(alliedRobots.length>4){//there must be allies nearby for defense
			MapLocation[] alliedPastrs =rc.sensePastrLocations(rc.getTeam());
			if(alliedPastrs.length<5&&(rc.readBroadcast(50)+60<Clock.getRoundNum())){//no allied robot can be building a pastr at the same time
				for(int i=0;i<20;i++){
					MapLocation checkLoc = VectorFunctions.mladd(rc.getLocation(),new MapLocation(randall.nextInt(8)-4,randall.nextInt(8)-4));
					if(rc.canSenseSquare(checkLoc)){
						double numberOfCows = rc.senseCowsAtLocation(checkLoc);
						if(numberOfCows>1000){//there must be a lot of cows there
							if(alliedPastrs.length==0){//there must not be another pastr nearby
								buildPastr(checkLoc);
							}else{
								MapLocation closestAlliedPastr = VectorFunctions.findClosest(alliedPastrs, checkLoc);
								if(closestAlliedPastr.distanceSquaredTo(checkLoc)>GameConstants.PASTR_RANGE*5){
									buildPastr(checkLoc);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void buildPastr(MapLocation checkLoc) throws GameActionException {
		rc.broadcast(50, Clock.getRoundNum());
		for(int i=0;i<100;i++){//for 100 rounds, try to build a pastr
			if(rc.isActive()){
				if(rc.getLocation().equals(checkLoc)){
					rc.construct(RobotType.PASTR);
				}else{
					Direction towardCows = rc.getLocation().directionTo(checkLoc);
					BasicPathing.tryToMove(towardCows, true,true, true);
				}
			}
			rc.yield();
		}
	}

	
}


