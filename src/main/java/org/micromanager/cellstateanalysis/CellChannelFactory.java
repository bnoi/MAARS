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
	 * @return the maxNbSpot
	 */
	public int getMaxNbSpot() {
		return maxNbSpot;
	}

	/**
	 * @return the spotRadius
	 */
	public double getSpotRadius() {
		return spotRadius;
	}

}
