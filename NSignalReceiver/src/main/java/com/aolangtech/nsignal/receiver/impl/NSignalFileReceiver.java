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
import com.aolangtech.nsignal.utils.OptionTradeHandlerUtil;

public class NSignalFileReceiver implements NSignalReceiver{
	
	// A logger
	private Logger logger = Logger.getLogger(NSignalFileReceiver.class);

	private String filename;
	private BufferedReader fileBr;
	private boolean isOpen;
	
	private OptionTradeHandlerUtil handler;
	
	@Override
	public void run() {
		if(!isOpen)
			this.open();
		
		handler = new OptionTradeHandlerUtil();
		
		try {
			String line;
			
			while((line = this.receiveRecord()) != null) {
				handler.handleOneLineRecord(line);
			}
			// after process
			handler.processForMap();
			
			// persist data
			handler.persist();
			
		} catch (NsignalException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Open the receiver.
	 * 
	 * @return true if open success, otherwise false.
	 */
	public boolean open() {
		InputStream in;
		try {
			in = new FileInputStream(this.filename);
			InputStreamReader inr = new InputStreamReader(in);
			fileBr = new BufferedReader(inr);
			isOpen = true;
			return true;
		} catch (FileNotFoundException e) {
			isOpen = false;
			return false;
		}
	}

	/**
	 * Receiver a option trade record of type of string line.
	 * 
	 * @return a record if there are records, otherwise null.
	 * @throws NsignalException
	 */
	public String receiveRecord() throws NsignalException {
		try {
			return fileBr.readLine();
		} catch (IOException e) {
			logger.error("Receive records failed in receiveing!");
			throw new NsignalException("Receive records failed in receiveing!");
		}
	}

	/**
	 * Close the receiver.
	 */
	public void close() {
		try {
			if(fileBr != null){
				fileBr.close();
			}
		} catch (IOException e) {
			logger.error("Close the receiver failed!");
		}
		isOpen = false;
	}
	
	public NSignalFileReceiver(String filename) {
		this.filename	=	filename;
	}

}
