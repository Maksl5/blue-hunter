/**
 *  ColorSystem.java in com.maksl5.bl_hunt.custom_ui
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.custom_ui;

import com.maksl5.bl_hunt.BlueHunter;
import com.maksl5.bl_hunt.R;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;


/**
 * @author Maksl5[Markus Bensing]
 *
 */
public class ColorSystem {

	public static Drawable getColoredBackground(BlueHunter blueHunter) {
		
		Drawable bg =  blueHunter.getResources().getDrawable(R.drawable.bg_main);
		
		float[] HSV = new float[3];
		Color.colorToHSV(Color.GREEN, HSV);
		
		bg.setColorFilter(adjustHue(HSV[0] - 196));
		return bg;
		
		
	}
	
	
	private static ColorFilter adjustHue( float value )
	{
	    ColorMatrix cm = new ColorMatrix();

	    adjustHue(cm, value);

	    return new ColorMatrixColorFilter(cm);
	}
	
	private static void adjustHue(ColorMatrix cm, float value)
	{
	    value = cleanValue(value, 180f) / 180f * (float) Math.PI;
	    if (value == 0)
	    {
	        return;
	    }
	    float cosVal = (float) Math.cos(value);
	    float sinVal = (float) Math.sin(value);
	    float lumR = 0.213f;
	    float lumG = 0.715f;
	    float lumB = 0.072f;
	    float[] mat = new float[]
	    { 
	            lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0, 
	            lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
	            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0, 
	            0f, 0f, 0f, 1f, 0f, 
	            0f, 0f, 0f, 0f, 1f };
	    cm.postConcat(new ColorMatrix(mat));
	}

	protected static float cleanValue(float p_val, float p_limit)
	{
	    return Math.min(p_limit, Math.max(-p_limit, p_val));
	}
	
	
	
}
