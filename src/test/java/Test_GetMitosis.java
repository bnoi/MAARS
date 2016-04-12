import static org.junit.Assert.*;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.micromanager.cellstateanalysis.GetMitosis;

public class Test_GetMitosis {

	GetMitosis getMitosis = new GetMitosis();
	String cwd = System.getProperty("user.dir") + "/resources";

	@Test
	public void testDistance() {
		double dis = getMitosis.distance(0, 0, 1, 1);
		assertEquals(1.414, dis, 0.001);
	}

	@Test
	public void testFilesExistNegativeNoFolder() {
		Boolean answer = getMitosis.filesExist("", 0, "CFP");
		assertEquals(false, answer);
	}

	@Test
	public void testFilesExistNegativeNoCell() {
		Boolean answer = getMitosis.filesExist(cwd, (int) FastMath.round(Double.POSITIVE_INFINITY), "CFP");
		assertEquals(false, answer);
	}
	
	@Test
	public void testFilesExistNegativeNoChannel() {
		Boolean answer = getMitosis.filesExist(cwd, 0, "");
		assertEquals(false, answer);
	}

	@Test
	public void testFilesExistPositive() {
		Boolean answer = getMitosis.filesExist(cwd, 0, "CFP");
		assertEquals(true, answer);
	}
	
	@Test
	public void testLoadROIsAnalaysis() {
		getMitosis.loadROIsAnalaysis(cwd + "/movie_X0_Y0/BF_Results.csv");
//		assertEquals(true, answer);
	}
	
}

