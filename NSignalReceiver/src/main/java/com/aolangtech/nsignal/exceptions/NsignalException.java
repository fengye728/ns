/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.exceptions;

public class NsignalException extends Exception{
	
	private static final long serialVersionUID = -3223382095355720258L;
	
	
	private String content;
	
	public NsignalException(String msg) {
		this.content = msg;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return this.content;
	}
}