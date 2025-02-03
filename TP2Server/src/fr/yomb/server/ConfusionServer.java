package fr.yomb.server;


import javacard.framework.Shareable;

public interface ConfusionServer extends Shareable {

	public short[] zShortToShort(short[] src);
	
	public byte[] zByteToShort(byte[] src);

	public short shortToZShort(short src);

	public Object objectToShort(Object src);

}
