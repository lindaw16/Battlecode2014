package bob;

import battlecode.common.*;

public class RobotPlayer
{
	public static void run(RobotController rc)
	{
		while(true)
		{
			//do things here
			if (rc.getType() == RobotType.HQ){
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
			} else if (rc.getType() == RobotType.SOLDIER) 
			  {				
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
				} else //no enemies, build a tower
				{
					if (Math.random() < 0.01)
					{
						if (rc.isActive())
						{
							try
							{
								rc.construct(RobotType.PASTR);
							} catch (GameActionException e)
							{
								e.printStackTrace();
							}

						}

					}
				}

				//movement
				Direction allDirections[] = Direction.values();
				Direction chosenDirection = allDirections[(int)(Math.random()*8)];
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
			rc.yield(); //ends the current round
		}
	}
}

