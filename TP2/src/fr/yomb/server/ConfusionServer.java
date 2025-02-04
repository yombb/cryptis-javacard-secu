package fr.yomb.server;


import javacard.framework.Shareable;

public interface ConfusionServer extends Shareable {

	short zShortToShort(short[] src);
	
	short zByteToShort(byte[] src);

	short[] shortToZShort(short src);

	short objectToShort(Object src);

}
