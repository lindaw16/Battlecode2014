package bub;

import java.util.Random;

import battlecode.common.*;

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
			if (rc.getType() == RobotType.HQ){
				runHeadquarters();
			}
			else if(rc.getType() == RobotType.SOLDIER){
				runSoldier();
			}
			
		}
	}
	private static void runSoldier()
	{
		
	}
	private static void runHeadquarters(){
		Direction spawnDir = Direction.NORTH;
		//if the robot is active and can move in the spawn direction, and there are less than maximum robots
		if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
		{
			rc.spawn(Direction.NORTH);
		}
	}
}