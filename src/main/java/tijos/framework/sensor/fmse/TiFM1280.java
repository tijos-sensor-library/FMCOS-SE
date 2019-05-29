package tijos.framework.sensor.fmse;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.util.BigBitConverter;

public class TiFM1280 {

	// 驱动对象
	private static TiFM1280 sefm1280;
	// 驱动对象
	private I2cDriver _driver;

	// 构造
	private TiFM1280() {

	}

	/**
	 * 获取引用
	 * 
	 * @return 对象
	 */
	public static TiFM1280 getInstance() {
		if (sefm1280 == null) {
			sefm1280 = new TiFM1280();
		}
		return sefm1280;
	}

	/**
	 * 初始化
	 * 
	 * @param i2c  通讯端口
	 * @param port 电源控制端口
	 * @param pin  电源控制线
	 */
	public void init(TiI2CMaster i2c, TiGPIO port, int pin) {
		this._driver = new I2cDriver(i2c, port, pin);
	}

	/**
	 * 上电操作
	 * 
	 * @return ATR数据
	 * @throws SEException
	 */
	public byte[] PowerOn() throws SEException {
		this._driver.openDev();
		return this._driver.getATR();
	}

	/**
	 * 下电操作
	 * 
	 * @throws SEException
	 */
	public void PowerOff() throws SEException {
		this._driver.closeDev();
	}

	/**
	 * 获取挑战随机数
	 * 
	 * @param rnd 随机数缓冲区
	 * @param len 获取长度
	 * @return SW码
	 * @throws SEException
	 */
	public int getChallenge(byte[] rnd, int len) throws SEException {
		int le = (len > 16) ? 16 : len;
		if (le > rnd.length) {
			throw new ArrayIndexOutOfBoundsException(le - rnd.length);
		}
		byte[] cmd = new byte[] { 0x00, (byte) 0x84, 0x00, 0x00, (byte) le };
		byte[] rsp = this._driver.transmitData(cmd);

		int ln = rsp.length - 2;
		System.arraycopy(rsp, 0, rnd, 0, ln);

		return BigBitConverter.ToUInt16(rsp, ln);
	}

	// ...其他方法

}
