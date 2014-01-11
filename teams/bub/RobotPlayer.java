/*
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
	public static int pastrCount;
	public static int robC;
	public static RobotInfo myInfo;
	public static currDest;
	public static int pathx[];
	public static int pathy[];
	
	public static void run(RobotController rcinput) throws GameActionException
	{
		rc = rcinput;
		randall.setSeed(rc.getRobot().getID());
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		distPASTR = (mapHeight + mapWidth) / 2 / 20;
		robC = rc.senseRobotCount();
		

		while(true)
		{
			
			try{
				
				if (rc.getType() == RobotType.HQ){
					runHeadquarters();
				}

				else if(rc.getType() == RobotType.SOLDIER){
					runSoldier();
				}

			
			}catch(GameActionException e){
				e.printStackTrace();
				
			}
		}
	}
	private static void runSoldier() throws GameActionException
	{
		if (robC%4 == 0){//spreadout
			simpleMove();
		}
		else if(robC%4 == 1){//normal soldier
			simpleMove();
		}
		else if(robC%4 == 2){//attack
			if (rc.canSenseObject(Robot.class)){
				
				GameObject enemies[] = rc.senseNearbyGameObjects(Robot.class);
				for (int i = 1;i<= enemies.length;i++){
					if (rc.canAttackSquare(sqr)&& enemy.getTeam()!= rc.getTeam()){
					
						rc.attackSquare(sqr);
					}
				}
				
			}
			else{
				simpleMove();
			}
			
		}
		else{//build pasture if less than 3 pastures
			if(pastrCount >=3){
				//Don't build if >= 3 PASTRS, or if soldier count is very low
				simpleMove();
				
			}
			else{
				
			}
		}
		
	}
	private static void runHeadquarters()throws GameActionException{
		Direction spawnDir = allDirections[(int)(Math.random()*8)];
		//if the robot is active and can move in the spawn direction, and there are less than maximum robots
		if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
		{
			rc.spawn(spawnDir);
			robC++;
		}
	}
<<<<<<< HEAD
	private static void simpleMove()throws GameActionException{
		//for now Random
		Direction spawnDir = allDirections[(int)(Math.random()*8)];
		rc.move(spawnDir);
		
		
		
	}
}
=======
}
*/
>>>>>>> ee0f487bb93eda77190d83be5cad44526b8f95e2
