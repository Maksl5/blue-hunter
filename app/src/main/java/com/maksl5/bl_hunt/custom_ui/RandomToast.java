/**
 *  RandomToast.java in com.maksl5.bl_hunt.custom_ui
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.custom_ui;



import java.util.Random;

import android.content.Context;
import android.widget.Toast;



/**
 * A {@link RandomToast} is basically a {@link Toast} which is only showed to the user with the specified propability.
 * 
 * @author Maksl5[Markus Bensing]
 */
public class RandomToast {

	private Context context;
	private CharSequence message;
	private double probability;

	private RandomToast(Context context,
			CharSequence message,
			double probability) {

		this.context = context;
		this.message = message;
		this.probability = probability;

	}

	/**
	 * Creates a new {@link RandomToast} instance to show.
	 * 
	 * @param context
	 *            The context in which the Toast will be created.
	 * @param message
	 *            The message shown to the user.
	 * @param probability
	 *            The probability the Toast will be shown on calling show().
	 * @return The created {@link RandomToast} instance.
	 */
	public static RandomToast create(	Context context,
										CharSequence message,
										double probability) {

		return new RandomToast(context, message, probability);
	}

	public void show() {

		if (new Random().nextDouble() <= probability) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

}
