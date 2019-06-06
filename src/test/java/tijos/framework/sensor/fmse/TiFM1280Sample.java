package tijos.framework.sensor.fmse;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.util.BigBitConverter;
import tijos.framework.util.Formatter;

public class TiFM1280Sample {

	public static void main(String[] args) {

		System.out.println("TiFM1280 test.");

		TiFM1280 se = TiFM1280.getInstance();

		try {

			TiGPIO gpio = TiGPIO.open(0, 8);
			TiI2CMaster i2c = TiI2CMaster.open(0);

			se.init(i2c, gpio, 8);

			byte[] atr = se.PowerOn();
			System.out.println("ATR:" + Formatter.toHexString(atr));

			for (int i = 0; i < 10; i++) {
				byte [] rnd  = se.getChallenge(16);
				System.out.println("RND:" + Formatter.toHexString(rnd));
			}
		
			
			byte [] finfo = se.selectFile(0x3f00);
			System.out.println("3f00 finfo:" + Formatter.toHexString(finfo));
			
			finfo = se.selectFile(0xdf01);
			System.out.println("df01 finfo:" + Formatter.toHexString(finfo));
			

			finfo = se.selectFile(0x0128);
			System.out.println("0x0128 finfo:" + Formatter.toHexString(finfo));

			byte [] bin = se.readBinary(0, 64);
			System.out.println("0x0128 bin:" + Formatter.toHexString(bin));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransactException e) {
			System.out.println("SE error:" + e.getReason());
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				se.PowerOff();
			} catch (TransactException e) {
				e.printStackTrace();

			}
		}

	}

}
