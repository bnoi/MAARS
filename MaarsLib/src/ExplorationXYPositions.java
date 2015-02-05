/**
 * ExplorationXYPositions give positions for exploration of an area following this pattern :
 *        X
 *   +--------------->
 *   |  1    6 -> 7
 *   |  |    ^    |
 *   |  v    |    v
 * Y |  2    5    8
 *   |  |    ^    |
 *   |  v    |    v
 *   |  3 -> 4    9
 *   |
 *   v
 * this is an example for 3 fields x and 3 fields y
 * 
 * @author marie
 *
 */
public class ExplorationXYPositions {
	
	private double[][] xyPositions;
	private int length;
	
	/**
	 * Constructor of ExplorationXYPositions,
	 * fieldWidth and fieldHeigth must be in microns
	 * 
	 * @param xNumber
	 * @param yNumber
	 * @param fieldWidth
	 * @param fieldHeigth
	 */
	public ExplorationXYPositions(int xNumber,int yNumber, double fieldWidth, double fieldHeigth) {
		
		length = xNumber*yNumber;
		xyPositions = new double[length][2];
		int n = 0;
		/*
		 * For each field following x axis :
		 */
		for (int x =0; x < xNumber; x++) {
			int y;
			int limit;
			int direction;
			/*
			 * if x is an even number direction is 1 meaning y is increasing
			 */
			if (x%2 == 0) {
				y = 0;
				limit = (int) yNumber;
				direction = 1;
			}
			/*
			 * if x is an odd number direction is -1 meaning y is decreasing
			 */
			else {
				y =  (int) yNumber - 1;
				limit = -1;
				direction = -1;
			}
			while (y - limit != 0){
				
				xyPositions[n][0] = x * fieldWidth;
				xyPositions[n][1] = y * fieldHeigth;
				
				y = y + direction;
				n++;
			}
		}
	}
	
	/**
	 * 
	 * @return number of position
	 */
	public int length() {
		return length;
	}
	
	/**
	 * 
	 * @param index
	 * @return double where first one is x coordinates and the second one is y coordinates
	 */
	public double[] get(int index) {
		return xyPositions[index];
	}
	
	/**
	 * 
	 * @param index
	 * @return x coordinates of position corresponding to index
	 */
	public double getX(int index) {
		return get(index)[0];
	}
	/**
	 * 
	 * @param index
	 * @return y coordinates of position corresponding to index
	 */
	public double getY(int index) {
		return get(index)[1];
	}
}
