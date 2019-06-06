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
	 * @throws TransactException
	 */
	public byte[] PowerOn() throws TransactException {
		this._driver.openDev();
		return this._driver.getATR();
	}

	/**
	 * 下电操作
	 * 
	 * @throws TransactException
	 */
	public void PowerOff() throws TransactException {
		this._driver.closeDev();
	}

	/**
	 * 获取挑战随机数
	 * 
	 * @param rnd 随机数缓冲区
	 * @param len 获取长度
	 * @return SW码
	 * @throws TransactException
	 * @throws ISOException 
	 */
	public byte [] getChallenge(int len) throws TransactException, ISOException {
		int le = (len > 16) ? 16 : len;

		byte[] cmd = new byte[] { 0x00, (byte) 0x84, 0x00, 0x00, (byte) le };
		
		byte [] tmp = this.deviceIo(cmd);
		return tmp;
	}

	/**
	 * Select file by file id
	 * @param fid file id 
	 * @return file data 
	 * @throws ISOException
	 * @throws TransactException
	 */
	public byte [] selectFile(int fid) throws ISOException, TransactException {
		
		byte [] b = BigBitConverter.GetBytes((short)fid);
		
		byte[] cmd = new byte[] { 0x00, (byte) 0xA4, 0x00, 0x00, (byte) b.length, b[0], b[1]};
		byte [] tmp = this.deviceIo(cmd);
		return tmp;
	}
	
	/**
	 * read binary from offset 
	 * @param offset
	 * @param len
	 * @return
	 * @throws ISOException
	 * @throws TransactException
	 */
	public byte [] readBinary(int offset, int len) throws ISOException, TransactException {

		byte [] b = BigBitConverter.GetBytes((short)offset);
		
		byte[] cmd = new byte[] { 0x00, (byte) 0xB0, b[0], b[1], (byte)len};
		byte [] tmp = this.deviceIo(cmd);
		return tmp;
		
	}

	/**
	 * Send command and return data 
	 * @param cmd
	 * @return response data
	 * @throws ISOException the exception is throw if sw != 0x9000
	 * @throws TransactException
	 */
	private byte [] deviceIo(byte [] cmd) throws ISOException, TransactException {
		byte[] rsp = this._driver.transmitData(cmd);

		int sw = BigBitConverter.ToUInt16(rsp, rsp.length -2);
		
		if(sw != 0x9000)
			throw new ISOException(sw);
	
		byte [] tmp = new byte[rsp.length - 2];
		System.arraycopy(rsp, 0, tmp, 0, rsp.length -2);
		
		return tmp;
		
	}
	
	

}
