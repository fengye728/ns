package com.aolangtech.nsignal.constants;

public class CommonConstants {

	public final static String	OPTION_TRADE_RECORD_SEPARATOR = ",";
	
	public final static int 	OPTION_TRADE_RECORD_FIELD_NUMBER = 14;
	
	public enum OptionType {
		CALL('C'), PUT('P');
		
		private char name;
		
		private OptionType(char name){
			this.name	=	name;
		}
		
		public static OptionType getEnumItem(char name){
			for(OptionType type : OptionType.values()){
				if(type.name == name){
					return type;
				}
			}
			return null;
		}

		@Override
		public String toString(){
			return String.valueOf(this.name);
		}
		
	}
	
	public enum TickTestTradeCategory {
		DOWNTICK(0), ZERO_DOWNTICK(1), ZERO_UPTICK(2), UPTICK(3);
		
		private int index;
		
		private TickTestTradeCategory(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return this.index;
		}
	}
}
