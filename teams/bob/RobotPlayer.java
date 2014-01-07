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
				rc.yield();
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
					//movement (prioritized)
					Direction allDirections[] = Direction.values();
					//I couldn't see the method for allDirections...
					//Direction chosenDirection = allDirections.(int)(Math.random()*8);
					/*
					if (rc.isActive() && rc.canMove(chosenDirection))
					{
						try{
							rc.move(chosenDirection);
						} catch(GameActionExceptione){
							e.printStackTrace();
						}
					*/
					
					//shooting
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
								//not sure if max or min squared ^^
								if (rc.isActive())
								{
									rc.attackSquare(anEnemyInfo.location);									
								}
							}
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else //no enemies
					  {
						//build a pasture
					  }
			  }		
		}
	}
}

