package bobette;
//will not build a pasture if there is not enough cows nearby
//will not build a pasture if another one is already closeby

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer
{
	public static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	
	public static void run(RobotController rcinput)
	{
		rc = rcinput;
		randall.setSeed(rc.getRobot().getID());
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
		//shooting (prioritized)
		//array of robots, attack the opponent team
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		if (enemyRobots.length> 0) //not empty
		{
			Robot anEnemy = enemyRobots[0];
			//need to use RobotInfo b/c Robot doesn't have location
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if (anEnemyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared)
			{
				if (rc.isActive())
				{
					rc.attackSquare(anEnemyInfo.location);									
				}
			}
		}else //no enemies, build a pasture
		{
			if (rc.isActive())
			{
				if (Math.random() < 0.01 && rc.senseCowsAtLocation(rc.getLocation()) > 5 ){
					Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 100000);
					boolean isPASTR = false;
					for (Robot r: nearbyRobots){
						RobotInfo rInfo;
						rInfo = rc.senseRobotInfo(r);
						isPASTR = rInfo.type == RobotType.PASTR;
						break;
					}
					if (!isPASTR){
						System.out.println("bobby is building pasture!");
						rc.construct(RobotType.PASTR);
					}
				}
			}
		}
		Direction chosenDirection = allDirections[(int)(randall.nextDouble()*8)];
		if (rc.isActive() && rc.canMove(chosenDirection))
			{
						rc.move(chosenDirection);
			}
		rc.yield(); //ends the current round
	}
	
	private static void runHeadquarters() throws GameActionException {
		Direction spawnDir = Direction.NORTH;
		//if the robot is active and can move in the spawn direction, and there are less than maximum robots
		if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
		{
			rc.spawn(Direction.NORTH);
		}
	}
}


