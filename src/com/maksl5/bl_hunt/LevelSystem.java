/**
 *  LevelSystem.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2012
 */
package com.maksl5.bl_hunt;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class LevelSystem {

	public static int getLevel(int exp) {

		int level = 1;

		int compareExp = 50;

		while (compareExp < exp) {
			compareExp = compareExp * 2 + compareExp;
			level += 1;
		}

		return level;
	}

	public static int getLevelStartExp(int level) {
		
		if(level == 1)
			return 0;
		
		
		int exp = 50;
		
		
		for(int i = 1;i < level - 1;i++)
		{
			exp = exp * 2 + exp;
		}
		
		return exp;
	}
	
	public static int getLevelEndExp (int level) {
		int exp = 50;
		
		for(int i = 1;i < level;i++)
		{
			exp = exp * 2 + exp;
		}
		
		
		return exp;
	}

}
