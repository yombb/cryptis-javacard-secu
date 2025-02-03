package fr.yomb.server;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Shareable;

public class ServerApplet extends Applet {

	public ConfusionServerImpl csi;
	
	public ServerApplet() {
		csi = new ConfusionServerImpl();
	}
	
	public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
		return csi;
	} 
	
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new ServerApplet().register(bArray, (short) (bOffset + 1),
				bArray[bOffset]);
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) 0x00:
			break;
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}
