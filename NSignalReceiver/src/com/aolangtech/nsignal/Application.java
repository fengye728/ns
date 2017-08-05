/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.model.OptionTradeRecordModel;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.receiver.impl.NSignalFileReceiver;

public class Application{
	
	static void receiveFromFile(String filename){
		
	}
	
	public static void main(String[] args){
		
		
		/*
		String inFilename = "D:\\test\\out24";
		NSignalReceiver nr = new NSignalFileReceiver(inFilename);
		
		nr.open();
		String line = nr.receiveRecord();
		OptionTradeRecordModel target = OptionTradeRecordModel.Parse(line);
		
		nr.close();
		*/
	} 
}