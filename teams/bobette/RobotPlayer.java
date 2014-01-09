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
	public static int mapHeight;
	public static int mapWidth;
	public static int distPASTR;
	
	public static void run(RobotController rcinput)
	{
		rc = rcinput;
		randall.setSeed(rc.getRobot().getID());
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		distPASTR = (mapHeight + mapWidth) / 2 / 20;
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
			Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, distPASTR);
			boolean isPASTR = false;
			for (Robot r: nearbyRobots){
				RobotInfo rInfo;
				try {
					rInfo = rc.senseRobotInfo(r);
					if(rInfo.type == RobotType.PASTR){
						isPASTR = true;
						break;
					}
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
			if (!isPASTR){
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


