import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.PointRoi;
import org.apache.commons.math3.util.FastMath;

public class Overlay_rect {
	public Overlay_rect() {
		ImagePlus img = IJ.openImage("/home/tong/Documents/movies/289/5/X0_Y0_FLUO/croppedImgs/49_GFP.tif");
		Line line = new Line(30, 30, 40, 50);
		Line.setWidth(5);
		img.setRoi(line);
		IJ.run(img, "Rotate...", "  angle=-30");
		PointRoi p = new PointRoi(31, 31);
//		img.setRoi(p);
		IJ.log("answers : " + line.contains((int)FastMath.round(p.getXBase() + p.getFloatWidth()), (int)FastMath.round(p.getYBase() + p.getFloatHeight())));
		img.show();
	}
	public static void main(String arg[]){
		new Overlay_rect();
	}
}