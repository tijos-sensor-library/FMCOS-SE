package tijos.framework.sensor.fmse;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
//import tijos.framework.util.Formatter;

class I2cDriver {

	// I/O外设对象
	private TiGPIO _gpio;
	private TiI2CMaster _i2cm;
	// 电源控制脚
	private int _pin;
	// 从机物理地址
	private int _address = 0x71;
	// 通讯缓冲区
	private byte[] _iobuffer = new byte[1024 + 2];

	// 帧数据发送
	private int sendFrame(int cmd, byte[] frame) {
		int flen = (frame != null) ? frame.length : 0;
		byte[] buff = this._iobuffer;
		int addr = this._address;
		int bcc = 0, pos = 0;

		buff[pos++] = (byte) (flen + 3);
		buff[pos++] = (byte) ((flen + 3) >> 8);
		buff[pos++] = 0;
		buff[pos++] = (byte) cmd;

		if (flen > 0) {
			System.arraycopy(frame, 0, buff, pos, flen);
			pos += flen;
		}

		for (int i = 0; i < pos; i++) {
			bcc ^= buff[i];
		}

		buff[pos++] = (byte) bcc;

		try {
			this._i2cm.write(addr, buff, 0, pos);
//			System.out.println("i2c.w:" + Formatter.toHexString(buff, 0, pos, " "));
		} catch (IOException e) {
//			e.printStackTrace();
			return 12;
		}

		return 0;
	}

	// 帧数据接收
	private byte[] recvFrame(int[] state) {
		byte[] frame = null;
		byte[] buff = this._iobuffer;
		int addr = this._address;
		int bcc = 0, total = 0;

		try {
			this._i2cm.read(addr, buff, 0, 2);
			int flen = (buff[0] & 0xff) | ((buff[1] & 0xff) << 8);
			if (flen < 3 || flen > 1024) {
				state[0] = 13;
				return frame;
			}
			total = flen + 2;
			this._i2cm.read(addr, buff, 0, total);
//			System.out.println("i2c.r:" + Formatter.toHexString(buff, 0, total, " "));

			for (int i = 0; i < total - 1; i++) {
				bcc ^= buff[i];
			}
			if ((byte) bcc != buff[total - 1]) {
				state[0] = 14;
				return frame;
			}
		} catch (IOException e) {
//			e.printStackTrace();
			state[0] = 12;
			return frame;
		}

		int flen = total - 5;
		frame = new byte[flen];
		System.arraycopy(buff, 4, frame, 0, flen);

		state[0] = (buff[3] & 0xff);
		return frame;
	}

	/**
	 * 构造
	 * 
	 * @param i2c  通讯端口
	 * @param gpio 电源控制端口
	 * @param pin  电源控制线
	 */
	I2cDriver(TiI2CMaster i2c, TiGPIO gpio, int pin) {
		this._i2cm = i2c;
		this._gpio = gpio;
		this._pin = pin;
	}

	/**
	 * 设备打开
	 * 
	 * @throws SEException
	 */
	void openDev() throws SEException {
		try {
			this._gpio.setWorkMode(this._pin, TiGPIO.OUTPUT_PP);
			this._gpio.writePin(this._pin, 0);
			this._i2cm.setWorkBaudrate(100);
		} catch (IOException e) {
			throw new SEException(255, e.getMessage());
		}
	}

	/**
	 * 驱动关闭
	 * 
	 * @throws SEException
	 */
	void closeDev() throws SEException {
		try {
			this._i2cm.close();
			this._gpio.writePin(this._pin, 1);
			this._gpio.close();
		} catch (IOException e) {
			throw new SEException(255, e.getMessage());
		}
	}

	/**
	 * 获取ATR数据
	 * 
	 * @return ATR
	 * @throws SEException
	 */
	byte[] getATR() throws SEException {
		int nRet = this.sendFrame(0x30, null); // I-BLOCK ATR请求块
		if (nRet != 0) {
			throw new SEException(nRet);
		}
		int time = (int) System.currentTimeMillis();
		int[] state = new int[1];
		byte[] recv = new byte[0];
		do {
			recv = this.recvFrame(state);
			if (state[0] != 0) {
				throw new SEException(state[0]);
			}
			int now = (int) System.currentTimeMillis();
			if (now - time > 6000) {
				break;
			}
		} while (recv == null);

		return recv;
	}

	/**
	 * 数据传输
	 * 
	 * @param buffer 发送数据缓冲区
	 * @return
	 * @throws SEException
	 */
	byte[] transmitData(byte[] buffer) throws SEException {
		int nRet = this.sendFrame(0x02, buffer); // I-BLOCK 数据块
		if (nRet != 0) {
			throw new SEException(nRet);
		}
		int time = (int) System.currentTimeMillis();
		int[] state = new int[1];
		byte[] recv = new byte[0];
		do {
			recv = this.recvFrame(state);
			if (state[0] == 12) {
				;
			} else if (state[0] == 13 || state[0] == 14) {
				this.sendFrame(0xBA, null); // R-BLOCK 请求重发
				time = (int) System.currentTimeMillis();
			} else if (state[0] == 0xF2) {
				time = (int) System.currentTimeMillis();
			} else {
				if (state[0] != 0) {
					throw new SEException(state[0]);
				}
			}
			int now = (int) System.currentTimeMillis();
			if (now - time > 6000) {
				break;
			}
		} while (recv == null);

		return recv;
	}

}
