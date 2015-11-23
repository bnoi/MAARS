package org.micromanager.cellstateanalysis;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 12, 2015
 */
public class CellChannelFactory {
	private String channel;
	private int maxNbSpot;
	private double spotRadius;

	public CellChannelFactory(String channelName, int maxNbSpot,
			double spotRadius) {
		this.channel = channelName;
		this.maxNbSpot = maxNbSpot;
		this.spotRadius = spotRadius;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the maxNbSpot
	 */
	public int getMaxNbSpot() {
		return maxNbSpot;
	}

	/**
	 * @param maxNbSpot
	 *            the maxNbSpot to set
	 */
	public void setMaxNbSpot(int maxNbSpot) {
		this.maxNbSpot = maxNbSpot;
	}

	/**
	 * @return the spotRadius
	 */
	public double getSpotRadius() {
		return spotRadius;
	}

	/**
	 * @param spotRadius
	 *            the spotRadius to set
	 */
	public void setSpotRadius(double spotRadius) {
		this.spotRadius = spotRadius;
	}
}
