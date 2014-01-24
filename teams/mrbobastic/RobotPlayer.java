package mrbobastic;

//things to do:
//defend pastrs that are under attack, or at least consider defending them
//battlecry when charging into battle -> concerted effort
//something like the opposite of a battlecry, when you're sure you're outnumbered

import java.util.ArrayList;
import java.util.Random;

//import mrbobastic.BreadthFirst;
//import mrbobastic.VectorFunctions;
import battlecode.common.*;

public class RobotPlayer{
	
	public static RobotController rc;
	public static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	public static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	
	
	static int bigBoxSize = 5;
	static MapLocation enemyHQ;
	
	//HQ data:
	static MapLocation rallyPoint;
	static MapLocation targetedPastr;
	static boolean die = false;
	
	//SOLDIER data:
//	static int myBand100 = 100;
//	static int myBand200 = 200;
	static int myBand = 100;
	static int pathCreatedRound = -1;
	
	
	static int numSoldiers = 10;
	static int countNumRobots = 0;
	
	static boolean pastrHQ = false;
	static boolean noiseTower = false;
	//static boolean herder = false;
	//static boolean attacker = false;
	static boolean soldier3000 = false;
	static boolean soldier100 = false;
	
	static boolean buildOrNot = false;
	static MapLocation pastrGoal = null;
	//static MapLocation enemyPastures[];
	//static MapLocation myPastures[];
	
//	static final int bigMapHerder = 14;
//	static final int bigMapBuilder = 10;
//	
//	static final int medMapHerder = 12;
//	static final int medMapBuilder = 8;
//	
//	static final int smallMapHerder = 9;
//	static final int smallMapBuilder = 5;
//	
//	static int mapHerderConst = 0;
//	static int mapBuilderConst = 0;
	
	static MapLocation mPoint; 
	
	static MapLocation pointsNoisetower[] = {};
	static int towerCount = 0;
	static int noiseConst = 24;
	
	public static int mapHeight;
	public static int mapWidth;
	

	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		Comms.rc = rcIn;
		randall.setSeed(rc.getRobot().getID());
		enemyHQ = rc.senseEnemyHQLocation();
		
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		mPoint = rc.getLocation();
		
		//System.out.println("creating robot "+rc.getType());


//		if((mapHeight + mapWidth) / 2 < 40){
//			mapHerderConst = smallMapHerder;
//			mapBuilderConst = smallMapBuilder;
//		}
//		else if((mapHeight + mapWidth) / 2 < 60){
//			mapHerderConst = medMapHerder;
//			mapBuilderConst = medMapBuilder;
//		}
//		else{
//			mapHerderConst = bigMapHerder;
//			mapBuilderConst = bigMapBuilder;
//		}
//		
//		
//		mapHerderConst += 2;
//		mapBuilderConst += 2;
		
		countNumRobots = rc.senseRobotCount();
		
		
//		if(countNumRobots > mapHerderConst){ //if there are more than 10 robots on the field, build pastrs
//			herder = true;
//			System.out.println("herder");
//		}
//		else if(countNumRobots > mapBuilderConst){
//			buildOrNot = true;
//			System.out.println("builder");
//		}
//		else if(countNumRobots == 1){
//			noiseTower = true;
//			System.out.println("noisetower");
//		}
//		
//		else if(countNumRobots == 2){
//			pastrHQ = true;
//			System.out.println("pastrHQ");
//		}
		//System.out.println("EHIHEIWE:RJEW " + rc.senseRobotCount());
		
		
		MapLocation[] ourPastrs = rc.sensePastrLocations(rc.getTeam());
		
		//if (rc.senseRobotCount() == 1)
		if (ourPastrs.length == 0)
		{
			pastrHQ = true;
			//buildOrNot = true;	
		}
		else if (countNumRobots == 2 || rc.getLocation().distanceSquaredTo(ourPastrs[0]) < 5)
		{
			//mPoint = rc.getLocation();
			System.out.println("HIHIHI");
			noiseTower = true;
		}
			
		else if (countNumRobots <= numSoldiers + 2) //stick around our pastr
		{
			myBand = 100;
			soldier100 = true;
		}

		else{
			soldier3000 = true;
			myBand = 3000;
			//System.out.println("attacker");
		}
		
		if(rc.getType()==RobotType.HQ){
			rc.broadcast(101,VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(),bigBoxSize)));//this tells soldiers to stay near HQ to start
			//rc.broadcast(102,-1);//and to remain in squad 1
			rc.broadcast(3001, VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(), bigBoxSize)));
			tryToSpawn();
			BreadthFirst.init(rc, bigBoxSize);
			rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
		}
		else if(rc.getType()==RobotType.NOISETOWER){
			recalculateFirst();
			//System.out.println("noiseTower");
		}
		else if(rc.getType()==RobotType.SOLDIER){
//			BreadthFirst.init(rc, bigBoxSize);
//			MapLocation goal = getRandomLocation();
//			path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
			//VectorFunctions.printPath(path,bigBoxSize);
			BreadthFirst.rc=rcIn;//slimmed down init
		}
		//MapLocation goal = getRandomLocation();
		//path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
		//VectorFunctions.printPath(path,bigBoxSize);
		

		while(true){
			try{
				if(rc.getType()==RobotType.HQ){
					runHQ();
					if(die)
						break;
				}else if(rc.getType()==RobotType.SOLDIER){
					runSoldier();
				} else if (rc.getType() == RobotType.NOISETOWER){
					runNoisetower();					
				}
			}catch (Exception e){
				//e.printStackTrace();
			}
			rc.yield();
		}
	}
	
	private static void runHQ() throws GameActionException {
		//TODO consider updating the rally point to an allied pastr 

		//first spawn as many little robots as possible
		//tryToSpawn();
		
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000000,rc.getTeam());
		
		//tell the guards to stay at our pastr
		MapLocation[] ourPASTRs = rc.sensePastrLocations(rc.getTeam());
		if (ourPASTRs.length > 0){
			//rc.broadcast(3000, 999);
			//rc.broadcast(3000, VectorFunctions.locToInt(VectorFunctions.mldivide(ourPASTRs[0], bigBoxSize)));
			
			//System.out.println("hq is broadcasting " + VectorFunctions.locToInt(VectorFunctions.mldivide(ourPASTRs[0], bigBoxSize)));
		}
		
		
		
		//if my team is defeated, regroup at main base:
		if(Clock.getRoundNum()>400&&alliedRobots.length<5){//call a retreat
			MapLocation startPoint = findAverageAllyLocation(alliedRobots);
			//Comms.findPathAndBroadcast(2,startPoint,rc.senseHQLocation(),bigBoxSize,2);
			Comms.findPathAndBroadcast(2, startPoint, rc.senseHQLocation(), bigBoxSize, 2);
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
			else 
			{
				rc.broadcast(5000, 888);
			}
		}
		
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		}
		
		//after telling them where to go, consider spawning
		tryToSpawn();
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
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());

		
//		if(rc.getHealth()<10){
//			System.out.println("self-destruct!");
//			rc.selfDestruct();
//		}
		
		if(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			if(enemyRobotLocations.length==0){//only HQ is in view
				navigateByPath(alliedRobots);
			}else{//shootable robots are in view
				MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
				boolean closeEnoughToShoot = closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared;
				if((alliedRobots.length+1)>=enemyRobots.length){//attack when you have superior numbers
					attackClosest(closestEnemyLoc);
				}else{//otherwise regroup
					regroup(enemyRobots,alliedRobots,closestEnemyLoc);
				}
			}
		}else{//NAVIGATION BY DOWNLOADED PATH
			if (soldier100)
			{
				//System.out.println("Soldier 100");
				navigateByPath(alliedRobots);
			}

			else if(pastrHQ){
				//System.out.println("EHLLO");
				//don't want to be too close to HQ
				MapLocation ourHQ = rc.senseHQLocation();
				if (rc.getLocation().distanceSquaredTo(ourHQ) < 5)
				{
					rc.move(rc.getLocation().directionTo(ourHQ).opposite());
				} else{
					//System.out.println("the pastr is at " + rc.getLocation().x + " " + rc.getLocation().y);
					rc.broadcast(3000, rc.getLocation().x * 100 + rc.getLocation().y);
					rc.construct(RobotType.PASTR);
				}
			}
			else if(noiseTower){
				//System.out.println("HEYYY");
				//MapLocation ourPASTR = rc.sensePastrLocations(rc.getTeam())[0];
				//rc.move(rc.getLocation().directionTo(ourPASTR));
				rc.construct(RobotType.NOISETOWER);
			}
			else if (soldier3000)
			{
				//System.out.println("Soldier 3000 read broadcast is " + rc.readBroadcast(3000));
//				int PastrBroadcast = rc.readBroadcast(3000);
//				int PastrHQx = PastrBroadcast / 100;
//				int PastrHQy = PastrBroadcast - PastrHQx * 100;
//				MapLocation PastrHQ = new MapLocation(PastrHQx, PastrHQy);
//				//System.out.println("and now I think the PastrHQ is at " + PastrHQ.x + " " + PastrHQ.y);
//				Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
//				simpleMove(towardPastrHQ);
				
//				MapLocation PastrHQ = protectThisPastr(rc.readBroadcast(3000));
//				Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
//				simpleMove(towardPastrHQ);
				
				protectPastr(rc.readBroadcast(3000));
				//rc.readBroadcast(5000);
			}
			
//			else if (herder)
//			{
//				//will do same thing as attacker for now
//				navigateByPath(alliedRobots);				
//			}
			else
			{
				System.out.println("BANANAS");
			}
			
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST
		
		//Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//simpleMove(towardEnemy);
		}
	}
	
	private static void navigateByPath(Robot[] alliedRobots) throws GameActionException{
		if(path.size()<=1){//
			//check if a new path is available
			int broadcastCreatedRound = rc.readBroadcast(myBand);
//			if (myBand == 3000)
//			{
//				System.out.println("my band is 3000 and my message is " + rc.readBroadcast(myBand));
//			}
			if(pathCreatedRound<broadcastCreatedRound){//download new place to go
				pathCreatedRound = broadcastCreatedRound;
				path = Comms.downloadPath();
			}else{//just waiting around. Consider building a pastr
				if (rc.readBroadcast(5000) == 888 || rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 5 || rc.senseRobotCount() > 10)
				{
					//no enemies around HQ
					//WE SHOULD PUT JACQUI'S PASTR FINDING CODE HERE
					System.out.println(rc.getRobot().getID() + " BUILD A PASTR");
					considerBuildingPastr(alliedRobots);
				}
				else 
				{
					//ACTUALLY I AM JUST MAKING THE ROBOT DEFEND OUR PASTR
					System.out.println (rc.getRobot().getID() + " PROTECT THE PASTRS");
//					MapLocation PastrHQ = protectThisPastr(rc.readBroadcast(3000));
//					Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
//					simpleMove(towardPastrHQ);
					protectPastr(3000);
				}

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
								if(closestAlliedPastr.distanceSquaredTo(checkLoc)>GameConstants.PASTR_RANGE*8){
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
					
//					MapLocation PastrHQ = protectThisPastr(rc.readBroadcast(3000));
//					if (rc.senseObjectAtLocation(PastrHQ) == null)
//					{
//						rc.broadcast(3000, rc.getLocation().x * 100 + rc.getLocation().y);			
//					}
					rc.construct(RobotType.PASTR);
					
				}else{
					Direction towardCows = rc.getLocation().directionTo(checkLoc);
					BasicPathing.tryToMove(towardCows, true,true, true);
				}
			}
			rc.yield();
		}
	}

	private static void regroup(Robot[] enemyRobots, Robot[] alliedRobots,MapLocation closestEnemyLoc) throws GameActionException {
		int enemyAttackRangePlusBuffer = (int) Math.pow((Math.sqrt(rc.getType().attackRadiusMaxSquared)+1),2);
		if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=enemyAttackRangePlusBuffer){//if within attack range, back up
			Direction awayFromEnemy = rc.getLocation().directionTo(closestEnemyLoc).opposite();
			BasicPathing.tryToMove(awayFromEnemy, true,true,false);
		}else{//if outside attack range, group up with allied robots
			MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc,false);
			MapLocation alliedRobotCenter = VectorFunctions.meanLocation(alliedRobotLocations);
			Direction towardAllies = rc.getLocation().directionTo(alliedRobotCenter);
			BasicPathing.tryToMove(towardAllies, true,true, false);
		}
	}

	private static void attackClosest(MapLocation closestEnemyLoc) throws GameActionException {
		//attacks the closest enemy or moves toward it, if it is out of range
		if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared){//close enough to shoot
			if(rc.isActive()){
				rc.attackSquare(closestEnemyLoc);
			}
		}else{//not close enough to shoot, so try to go shoot
			Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
			//simpleMove(towardClosest);
			BasicPathing.tryToMove(towardClosest, true,true, false);
		}
	}

	private static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		if(rc.isActive()){
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
	
	private static void recalculateFirst(){
//		MapLocation mPoint = rc.getLocation();
//		
//		pointsNoisetower = new MapLocation[8];
//	
//		for(int i=0; i<8; i++){
//			double constant = Math.pow((double)Math.abs(allDirections[i].dx) + (double)Math.abs(allDirections[i].dy), .5);
//			
//			int xVal = allDirections[i].dx;
//			int yVal = allDirections[i].dy;
//			int xValue = Math.max(0, (int)(mPoint.x + xVal*noiseConst / constant));
//			int yValue = Math.max(0, (int)(mPoint.y + yVal*noiseConst / constant));
//			xValue = Math.min(xValue, mapWidth);
//			yValue = Math.min(yValue, mapHeight);
//			pointsNoisetower[i] = new MapLocation(xValue, yValue);
//		}
		
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
		//System.out.println(xValue+" "+yValue);
		pointsNoisetower[index] = new MapLocation(xValue, yValue);

	}
	
//	private static MapLocation protectThisPastr(int loc) throws GameActionException
//	{
//		//int PastrBroadcast = rc.readBroadcast(3000);
//		int PastrHQx = loc / 100;
//		int PastrHQy = loc - PastrHQx * 100;
//		MapLocation PastrHQ = new MapLocation(PastrHQx, PastrHQy);
//		//System.out.println("and now I think the PastrHQ is at " + PastrHQ.x + " " + PastrHQ.y);
////		Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
////		simpleMove(towardPastrHQ);
//		return PastrHQ;
//	}
	
	
	private static void protectPastr(int loc) throws GameActionException
	{
		//int PastrBroadcast = rc.readBroadcast(3000);
		int PastrHQx = loc / 100;
		int PastrHQy = loc - PastrHQx * 100;
		MapLocation PastrHQ = new MapLocation(PastrHQx, PastrHQy);
		//System.out.println("and now I think the PastrHQ is at " + PastrHQ.x + " " + PastrHQ.y);
//		Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
//		simpleMove(towardPastrHQ);
		Direction towardPastrHQ = rc.getLocation().directionTo(PastrHQ);
		simpleMove(towardPastrHQ);
	}
	
	private static void runNoisetower() throws GameActionException {
//		towerCount %= 8;
//		if(towerCount == 0){
//			noiseConst -= 1;
//			recalculateFirst();
//		}
//		
//		towerCount++;
//		if(noiseConst <= 5){
//			noiseConst = 24;
//		}
//		rc.attackSquare(pointsNoisetower[towerCount]);
		
		noiseConst--;
		
		if(noiseConst <= 3){
			noiseConst = 24;
			towerCount++;
		}
		towerCount %= 8;
		recalculateOne(towerCount);
		//System.out.println(noiseConst);
		rc.attackSquare(pointsNoisetower[towerCount]);
	}
	
	
}