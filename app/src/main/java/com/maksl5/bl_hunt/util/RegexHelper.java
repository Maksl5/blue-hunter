/**
 *  RegexHelper.java in com.maksl5.bl_hunt.util
 *  © Markus 2013
 */
package com.maksl5.bl_hunt.util;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author Markus
 * 
 */
class RegexHelper {

	public static List<String> findGroups(	String regex,
										String searchIn) {

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(searchIn);

		List<String> groupList = new ArrayList<>();
		
		if(matcher.find()) {
			
			for (int i = 1; i < matcher.groupCount(); i++) {
				try {
					groupList.add(matcher.group(i));
				}
				catch (IllegalStateException e) {
					
				}
				
			}
			
			
		}
		
		return groupList;
		
	}
}
