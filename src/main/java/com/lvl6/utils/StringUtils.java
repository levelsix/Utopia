package com.lvl6.utils;

public class StringUtils {
	public static String displayName(String fieldName) {
		StringBuffer buff = new StringBuffer();
		buff.append(fieldName.substring(0, 1).toUpperCase());
		for(int i=1; i < fieldName.length(); i++) {
			char ci = fieldName.charAt(i);
			if(Character.isUpperCase(ci)) {
				buff.append(" ");
			}
			buff.append(ci);
		}
		return buff.toString();
	}
}
