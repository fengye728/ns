package com.aolangtech.nsignal.receiver.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.exceptions.NsignalException;
import com.aolangtech.nsignal.receiver.NSignalReceiver;

public class NSignalFileReceiver implements NSignalReceiver{
	
	// A logger
	private Logger logger = Logger.getLogger(NSignalFileReceiver.class);

	private String filename;
	private BufferedReader fileBr;
	
	@Override
	public boolean open() {
		InputStream in;
		try {
			in = new FileInputStream(this.filename);
			InputStreamReader inr = new InputStreamReader(in);
			fileBr = new BufferedReader(inr);
			return true;
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	@Override
	public String receiveRecord() throws NsignalException {
		try {
			return fileBr.readLine();
		} catch (IOException e) {
			logger.error("Receive records failed in receiveing!");
			throw new NsignalException("Receive records failed in receiveing!");
		}
	}

	@Override
	public void close() {
		try {
			if(fileBr != null){
				fileBr.close();
			}
		} catch (IOException e) {
			logger.error("Close the receiver failed!");
		}
		
	}
	
	public NSignalFileReceiver(String filename) {
		this.filename	=	filename;
		
	}

}
