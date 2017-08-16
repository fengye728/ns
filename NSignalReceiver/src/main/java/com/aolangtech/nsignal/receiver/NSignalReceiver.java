package com.aolangtech.nsignal.receiver;

import com.aolangtech.nsignal.exceptions.NsignalException;

public interface NSignalReceiver {

	/**
	 * Run the receiver.
	 * 
	 */
	public void run() throws NsignalException;
}
