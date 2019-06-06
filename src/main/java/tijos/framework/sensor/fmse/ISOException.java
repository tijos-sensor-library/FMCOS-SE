package tijos.framework.sensor.fmse;

public class ISOException extends Exception{

	int sw;
	
	public ISOException(int sw){
		this.sw = sw;
	}
	
	public int getReason() {
		return this.sw;
	}
	
	
	
}
