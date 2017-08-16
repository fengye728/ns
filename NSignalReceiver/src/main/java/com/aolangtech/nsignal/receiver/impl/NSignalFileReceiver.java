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
import com.aolangtech.nsignal.utils.OptionTradeHandlerContext;

public class NSignalFileReceiver implements NSignalReceiver{
	
	// A logger
	private Logger logger = Logger.getLogger(NSignalFileReceiver.class);

	private String filename;
	private BufferedReader fileBr;
	private boolean isOpen;
	
	private OptionTradeHandlerContext handler;
	
	@Override
	public void run() throws NsignalException {
		this.open();
		
		handler = new OptionTradeHandlerContext();
		
		String line;
		long recordCount = 0;
		while((line = this.receiveRecord()) != null) {
			handler.handleOneLineRecord(line);
			++recordCount;
		}
		
		logger.info("Load records success. Count: " + recordCount);
		// after process
		handler.processForMap();
		
		// persist data
		recordCount = handler.persist();
		logger.info("Persist " + handler.getOptionTradeDate() + " records success. Count: " + recordCount);
	}
	
	/**
	 * Open the receiver.
	 * @throws NsignalException 
	 * 
	 */
	public void open() throws NsignalException {
		if(isOpen)
			return ;
		
		InputStream in;
		try {
			in = new FileInputStream(this.filename);
			InputStreamReader inr = new InputStreamReader(in);
			fileBr = new BufferedReader(inr);
			isOpen = true;
		} catch (FileNotFoundException e) {
			isOpen = false;
			throw new NsignalException("Open file: " + this.filename + " failed!");
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
			throw new NsignalException("Receive records failed!");
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
