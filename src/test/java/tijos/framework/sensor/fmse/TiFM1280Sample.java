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

			byte[] rnd = new byte[16];
			for (int i = 0; i < 10; i++) {
				int sw = se.getChallenge(rnd, rnd.length);
				System.out.println("RND:" + Formatter.toHexString(rnd));
				System.out.println("SW:" + Formatter.toHexString(BigBitConverter.GetBytes((short) sw)));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SEException e) {
			System.out.println("SE error:" + e.getReason());
		} finally {
			try {
				se.PowerOff();
			} catch (SEException e) {
				e.printStackTrace();

			}
		}

	}

}
