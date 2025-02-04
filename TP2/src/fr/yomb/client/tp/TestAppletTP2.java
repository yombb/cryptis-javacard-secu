package fr.yomb.client.tp;

import fr.yomb.server.ConfusionServer;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.RandomData;

public class TestAppletTP2 extends Applet {

	private static final byte INS_CHECK_PIN = (byte) 0x40;
	
	private static final byte INS_OBJECT_TO_SHORT = (byte) 0x50;
	private static final byte INS_SHORT_TO_ZSHORT = (byte) 0x52;
	private static final byte INS_ZSHORT_TO_SHORT = (byte) 0x54;
	private static final byte INS_ZBYTE_TO_SHORT = (byte) 0x56;
	
	OwnerPIN pin;
	byte[] bA;
	byte[] transientBA;
	short sA[];
	Object o;
	RandomData rd;
	
	ConfusionServer cs;
	private final static byte[] aid = {(byte) 0x66, (byte) 0x72, (byte) 0x2E, (byte) 0x79, (byte) 0x6F, (byte) 0x6D, (byte) 0x62, 
		(byte) 0x2E, (byte) 0x73, (byte) 0x65, (byte) 0x72, (byte) 0x76, (byte) 0x65, (byte) 0x72, (byte) 0x01};
	
	public TestAppletTP2() {
		// On va faire toutes les allocations de mémoire ici
		
		// On créé un tableau transient (ie. tableau en RAM, effacé dès que l'applet est déselectionnée)
		transientBA = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);
		
		// On créé des tableaux de byte et de short persistent ainsi qu'un objet lambda
		sA = new short[28];
		o = new Object();
		bA = new byte[16];
		
		// On génère 8 octets aléatoire pour initialiser notre PIN
		rd = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
		rd.generateData(transientBA, (short) 0, (short) 8);
		
		pin = new OwnerPIN((byte) 5, (byte) 8);
		pin.update(transientBA, (byte) 0, (byte) 8);
		
	}
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new TestAppletTP2().register(bArray, (short) (bOffset + 1),
				bArray[bOffset]);
		
	}

	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}
		if (cs == null)
			cs = (ConfusionServer) JCSystem.getAppletShareableInterfaceObject(
				JCSystem.lookupAID(aid, (short) 0, (byte) aid.length), (byte) 0);

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case INS_CHECK_PIN:
		{
			short lc = apdu.setIncomingAndReceive();
			if (pin.check(buf, ISO7816.OFFSET_CDATA, (byte)lc))
				ISOException.throwIt(ISO7816.SW_NO_ERROR);
			else
				ISOException.throwIt((short) (0x6600+pin.getTriesRemaining()));
			break;
		}
		case INS_OBJECT_TO_SHORT:
		{
			Util.setShort(buf, (short) 0, cs.objectToShort(o));
			short le = apdu.setOutgoing();
			apdu.setOutgoingLength(le);
			apdu.sendBytes((short) 0, le);
			break;
		}
		case INS_SHORT_TO_ZSHORT:
		{
			short[] lZS = cs.shortToZShort(Util.getShort(buf, ISO7816.OFFSET_P1));
			if (lZS != null) {			
				short le = apdu.setOutgoing();
				for (short l = 0; (l<le); l+=2) {
					//Util.setShort(buf, l, returned[(short) (l>>1)]);
					buf[l] = (byte) ((lZS[(short) (l>>1)] >> 8) & 0xFF);
					buf[(short) l+1] = (byte) (lZS[(short) (l>>1)] & 0xFF); 
				}			
				apdu.setOutgoingLength(le);
				apdu.sendBytes((short) 0, le);
			}
			break;
		}
		case INS_ZSHORT_TO_SHORT:
		{
			Util.setShort(buf, (short) 0, cs.zShortToShort(sA));
			short le = apdu.setOutgoing();
			apdu.setOutgoingLength(le);
			apdu.sendBytes((short) 0, le);
			break;
		}
		case INS_ZBYTE_TO_SHORT:
		{
			Util.setShort(buf, (short) 0, cs.zByteToShort(bA));
			
			short le = apdu.setOutgoing();
			apdu.setOutgoingLength(le);
			apdu.sendBytes((short) 0, le);
			break;
		}
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}
