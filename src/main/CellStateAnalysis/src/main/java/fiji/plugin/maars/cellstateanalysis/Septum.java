package fiji.plugin.maars.cellstateanalysis;

/**
 * Class to store features of a septum : - length - width - angleToMajorAxis :
 * angle relative to major axis - positionToMajorAxis : position where septum
 * and major axis cross each other
 * 
 * @author marie
 *
 */
public class Septum {

	private double length;
	private double width;
	private double angleToMajorAxis;
	private double positionToMajorAxis;

	/**
	 * Constructor :
	 * 
	 * @param lenght
	 * @param width
	 * @param angleToMajorAxis
	 * @param positionToMajorAxis
	 */
	public Septum(double lenght, double width, double angleToMajorAxis,
			double positionToMajorAxis) {

		this.length = lenght;
		this.width = width;
		this.angleToMajorAxis = angleToMajorAxis;
		this.positionToMajorAxis = positionToMajorAxis;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}

	public double getAngleToMajorAxis() {
		return angleToMajorAxis;
	}

	public double getPositionToMajorAxis() {
		return positionToMajorAxis;
	}

	public void setLength(double newLength) {
		length = newLength;
	}

	public void setWidth(double newWidth) {
		width = newWidth;
	}

	public void setAngleToMajorAxis(double newAngle) {
		angleToMajorAxis = newAngle;
	}

	public void getPositionAxis(double newPosition) {
		positionToMajorAxis = newPosition;
	}
}
