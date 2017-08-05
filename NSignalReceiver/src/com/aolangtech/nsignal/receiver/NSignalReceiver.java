package com.aolangtech.nsignal.receiver;

public interface NSignalReceiver {
	
	public boolean open();
	
	public String receiveRecord();
	
	public void close();
}
