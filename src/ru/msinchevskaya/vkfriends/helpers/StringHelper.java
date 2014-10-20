package ru.msinchevskaya.vkfriends.helpers;

public class StringHelper {
	
	private StringHelper(){}
	
	public static final boolean isNumber(String string){
		for (int i = 0; i < string.length(); i++){
			final char c = string.charAt(i);
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

}
