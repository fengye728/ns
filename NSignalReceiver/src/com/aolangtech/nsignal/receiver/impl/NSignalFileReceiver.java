package com.aolangtech.nsignal.receiver.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.aolangtech.nsignal.receiver.NSignalReceiver;

public class NSignalFileReceiver implements NSignalReceiver{

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
	public String receiveRecord() {
		try {
			return fileBr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void close() {
		try {
			if(fileBr != null){
				fileBr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public NSignalFileReceiver(String filename) {
		this.filename	=	filename;
		
	}

}
