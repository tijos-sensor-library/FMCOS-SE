package tijos.framework.sensor.fmse;

@SuppressWarnings("serial")
public class SEException extends Exception {

	/**
	 * SE数据校验错误
	 */
	public static final int SE_ERR_CRC = 1;
	/**
	 * SE不支持的命令
	 */
	public static final int SE_ERR_INS = 2;
	/**
	 * 接口无应答
	 */
	public static final int IF_ERR_RECV_ACK = 12;
	/**
	 * 接口数据长度错误
	 */
	public static final int IF_ERR_LENGTH = 13;
	/**
	 * 接口数据校验错误
	 */
	public static final int IF_ERR_LRC = 14;

	/**
	 * 接口未知错误
	 */
	public static final int IF_ERR_UNKONWN = 255;

	// 异常原因码
	private int reason;

	/**
	 * 构造
	 * 
	 * @param reason 原因码
	 */
	public SEException(int reason) {
		this.reason = reason;
	}

	/**
	 * 构造
	 * 
	 * @param reason  原因码
	 * @param message 消息
	 */
	public SEException(int reason, String message) {
		super(message);
		this.reason = reason;
	}

	/**
	 * 获取异常原因
	 * 
	 * @return 原因码
	 */
	public int getReason() {
		return this.reason;
	}
}
