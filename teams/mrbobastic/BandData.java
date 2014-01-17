package mrbobastic;

public class BandData{
	
	public static int round;
	public static int size;
	public static int x;
	public static int y;
	
	
	public BandData(int startRound, int startSize, int startX, int startY)
	{
		round = startRound;
		size = startSize;
		x = startX;
		y = startY;
	}
	
	
	public int concatenate()
	{
		int message = round * 100000 + size * 10000 + x * 100 + y * 1;
		return message;
	}
	
//BandData Eunice = new BandData(1, 2, 34, 56);
//	
//	public static void main(String[] args){
//		BandData Eunice = new BandData(1, 2, 34, 56);
//		System.out.println(Eunice.concatenate());
//	}

}
	