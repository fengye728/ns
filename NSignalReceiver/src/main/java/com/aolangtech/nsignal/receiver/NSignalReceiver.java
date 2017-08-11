package com.aolangtech.nsignal.receiver;

import com.aolangtech.nsignal.exceptions.NsignalException;

public interface NSignalReceiver {
	
	/**
	 * Open the receiver.
	 * 
	 * @return true if open success, otherwise false.
	 */
	public boolean open();
	
	/**
	 * Receiver a option trade record of type of string line.
	 * 
	 * @return a record if there are records, otherwise null.
	 * @throws NsignalException
	 */
	public String receiveRecord() throws NsignalException;
	
	/**
	 * Close the receiver.
	 */
	public void close();
}
