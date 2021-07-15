package org.erasmusmc.rremanager.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtilities {
	
	
	public static String escapeFieldValue(String value) {
		return escapeFieldValue(value, ",", "\"");
	}
	
	
	public static String escapeFieldValue(String value, String fieldDelimiter, String textQualifier) {
		if (value == null) {
			value = "";
		}
		else if (value.contains(fieldDelimiter) || value.contains(textQualifier)) {
			value = textQualifier + value.replaceAll(textQualifier, textQualifier + textQualifier) + textQualifier;
		}
		return value;
	}
	
	
	public static String unEscapeFieldValue(String value) {
		return unEscapeFieldValue(value, ",", "\"");
	}
	
	
	public static String unEscapeFieldValue(String value, String fieldDelimiter, String textQualifier) {
		if (value == null) {
			value = "";
		}
		else if (value.startsWith(textQualifier) && value.endsWith(textQualifier) && value.length() > 1) {
			value = value.substring(1, value.length() - 1).replaceAll(textQualifier + textQualifier, textQualifier);
		}
		return value;
	}
	
	
	public static List<String> intelligentSplit(String string, char separator, char textQualifier) throws Exception {
		List<String> split = new ArrayList<String>();
		
		if (string.length() > 0) {
			boolean quoted = false;
			String segment = "";
			int characterNr = 0;
			char nextCharacter = string.charAt(0);
			while (characterNr < string.length()) {
				char character = nextCharacter;
				nextCharacter = (characterNr + 1) < string.length() ? string.charAt(characterNr + 1) : '\0';
				
				if (quoted) {
					if (character == textQualifier) {
						if (nextCharacter != '\0') {
							if (nextCharacter == textQualifier) {
								segment += textQualifier;
							}
							else {
								quoted = false;
							}
						}
						else {
							throw new Exception("Unexpected end of string");
						}
					}
					else {
						segment += character;
					}
				}
				else {
					if (character == separator) {
						split.add(segment);
						segment = "";
					}
					else if (character == textQualifier) {
						quoted = true;
					}
					else {
						segment += character;
					}
				}
				characterNr++;
			}
			split.add(segment);
		}
		else {
			split.add("");
		}
		
		return split;
	}
	
	public static String join(Collection<?> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<?> iter = s.iterator();
		if (iter.hasNext()) {
			buffer.append(iter.next().toString());
		}
		while (iter.hasNext()) {
			buffer.append(delimiter);
			buffer.append(iter.next().toString());
		}
		return buffer.toString();
	}
	
	public static String join(Object[] objects, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		if (objects.length != 0)
			buffer.append(objects[0].toString());
		for (int i = 1; i < objects.length; i++) {
			buffer.append(delimiter);
			buffer.append(objects[i].toString());
		}
		return buffer.toString();
	}

}
