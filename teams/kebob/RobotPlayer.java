package kebob;
//will not build a pasture if there is not enough cows nearby
//will not build a pasture if another one is already close by
//spawn in ALL THE DIRECTIONS
//don't build more than numPASTR pastures

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer
{
	public static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	public static int mapHeight;
	public static int mapWidth;
	public static int distPASTR;
	public static int numPASTR = 7;
	static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	
	public static void run(RobotController rcinput)
	{
		rc = rcinput;
		randall.setSeed(rc.getRobot().getID());
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		distPASTR = (mapHeight + mapWidth) / 2 / 20 + 3;
		while(true)
		{
			try{
				if (rc.getType() ==RobotType.HQ)
				{
					runHeadquarters();				
				} else if (rc.getType() == RobotType.SOLDIER)
				  {
					runSoldier();				
				  }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
			
			
	private static void runSoldier() throws GameActionException{
		//shooting (prioritized) NOPE
		//array of robots, attack the opponent team
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		Robot[] teamRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		
		//first build numPASTR of PASTRs
		int totalPASTRs = 0;
		for (Robot rTeam: teamRobots){
			if (rc.senseRobotInfo(rTeam).type == RobotType.PASTR)
			{
				totalPASTRs++;
			}
		}
		//System.out.println(totalPASTRs);
		if (totalPASTRs < numPASTR)
		{
			Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, distPASTR);
			boolean isPASTR = false;
			for (Robot rOpp: nearbyRobots){
				RobotInfo rInfo;
				rInfo = rc.senseRobotInfo(rOpp);
				if(rInfo.type == RobotType.PASTR || rInfo.type == RobotType.HQ){
					isPASTR = true;
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
		} else { 
			//System.out.println("STOPMAKINGPASTRS");
			//System.out.println(totalPASTRs);
		}
		// enough PASTRs! Move as a swarm and find opponents to shoot
		
		
		if (enemyRobots.length> 0) //there are enemies nearby
		{
			Robot anEnemy = enemyRobots[0];
			//need to use RobotInfo b/c Robot doesn't have location
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			/*
			if (anEnemyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared)
			{
				if (rc.isActive())
				{
					rc.attackSquare(anEnemyInfo.location);									
				}
			}
			*/
			swarmMove(anEnemyInfo.location);
		} else //no enemies, move somewhere else
		  {
			Direction chosenDirection = allDirections[(int)(randall.nextDouble()*8)];
			if (rc.isActive() && rc.canMove(chosenDirection))
			{
				rc.move(chosenDirection);
			}
			rc.yield(); //ends the current round
		  }
	}
	
	private static void runHeadquarters() throws GameActionException {
		//Direction spawnDir = Direction.NORTH;
		//if the robot is active and can move in the spawn direction, and there are less than maximum robots
		if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
		{
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
	
	private static void swarmMove(MapLocation averagePositionOfSwarm) throws GameActionException{
		Direction chosenDirection = rc.getLocation().directionTo(averagePositionOfSwarm);
		if(rc.isActive() && rc.getType() != RobotType.PASTR){
			if(randall.nextDouble()<0.5){//go to swarm center
				for(int directionalOffset:directionalLooks){
					int forwardInt = chosenDirection.ordinal();
					Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
					if(rc.canMove(trialDir)){
						rc.move(trialDir);
						break;
					}
				}
			} else{//go wherever the wind takes you
				Direction d = allDirections[(int)(randall.nextDouble()*8)];
				if(rc.isActive() && rc.canMove(d)){
					rc.move(d);
				}
			}
		}
	}
	
	
}




