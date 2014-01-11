package tiffany;
//will not build a pasture if there is not enough cows nearby
//will not build a pasture if another one is already closeby

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer
{
	//hq stuff
		public static int lastSpawnRound = -10000;
		//soldier stuff
		public static MapLocation enemy;
		public static MapLocation myLoc;
		public static int myID;
		public static int height;
		public static int width;
		public static ArrayList<Direction> path = new ArrayList<Direction>();
		public static Direction[] dirs = {Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
		public static Direction[] orthoDirs = {Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
		public static int[] dirSearch = new int[]{0,1,-1,2,-2,-3,3,4};
		public static int[][] minionData;
		public static boolean minionDataDownloaded=false;
		public static boolean positiveFollow = true;
		public static int lastNode = -1;
		public static int currentNode = -1;
	
	public static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	public static int mapHeight;
	public static int mapWidth;
	public static int distPASTR = 1;
	
	public static void run(RobotController rcinput)
	{
		rc = rcinput;
		randall.setSeed(rc.getRobot().getID());
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		if((mapHeight + mapWidth) / 2 <=40){
			distPASTR = 10;
		}
		else {
			distPASTR = (mapHeight + mapWidth) / 2 / 5;
		}
		System.out.println(mapHeight+" "+mapWidth);
		
		
		while(true)
		{	
			try {
				if (rc.getType() ==RobotType.HQ)
				{
					runHeadquarters();				
				} 
				else if (rc.getType() == RobotType.SOLDIER)
				{
					runSoldier();				
				}
			}
			catch(Exception e){
					System.out.println(e);
					e.printStackTrace();
			}
			
		}
	}
			
			
	private static void runSoldier(){
		soldierShoot();
		soldierHerd();
		soldierMove();
		rc.yield(); //ends the current round
	}
	
	private static boolean nearPASTR() {
		Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, distPASTR);
		boolean isPASTR = false;
		for (Robot r: nearbyRobots){
			RobotInfo rInfo;
			try {
				rInfo = rc.senseRobotInfo(r);
				if(rInfo.type == RobotType.PASTR){
					return true;
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}							
		}
		return false;
	}
	
	private static void soldierHerd(){
		if(nearPASTR()){
			
		}
		
	}
	
	
	private static void soldierShoot(){
		//shooting (prioritized)
		//array of robots, attack the opponent team
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		if (enemyRobots.length> 0) //not empty
		{
			Robot anEnemy = enemyRobots[0];
			//need to use RobotInfo b/c Robot doesn't have location
			RobotInfo anEnemyInfo;
			try {
				anEnemyInfo = rc.senseRobotInfo(anEnemy);
				if (anEnemyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared)
				{
					if (rc.isActive())
					{
						rc.attackSquare(anEnemyInfo.location);									
					}
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else //no enemies, build a pasture
		 {  
			
			
			if (!nearPASTR()){
				if (Math.random() < 0.01 && rc.isActive())
				{
					System.out.println("tiffany is building pasture!");
					try
					{
						rc.construct(RobotType.PASTR);
					}
					catch (GameActionException e)
					{
						e.printStackTrace();
					}
				}


			}
		 }	
	}
	
	private static void runHeadquarters(){
		Direction spawnDir = Direction.NORTH;
		try
		{
			//if the robot is active and can move in the spawn direction, and there are less than maximum robots
			if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
			{
				rc.spawn(Direction.NORTH);
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void soldierMove(){
		//movement
		Direction chosenDirection = allDirections[(int)(randall.nextDouble()*8)];
		//returns a random direction from the array of directions
		if (rc.isActive() && rc.canMove(chosenDirection))
		{
			try{
				rc.move(chosenDirection);
			} catch(GameActionException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void tryToSpawn() throws GameActionException {
		//if(Clock.getRoundNum()>=lastSpawnRound+GameConstants.HQ_SPAWN_DELAY_CONSTANT_1){
		if(rc.isActive()&&rc.canMove(dirs[0])&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			rc.spawn(dirs[0]);
			lastSpawnRound=Clock.getRoundNum();
		}
		//}
	}
	
	
}