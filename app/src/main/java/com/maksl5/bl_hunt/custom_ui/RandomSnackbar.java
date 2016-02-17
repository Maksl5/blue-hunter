/**
 * RandomSnackbar.java in com.maksl5.bl_hunt.custom_ui
 * © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt.custom_ui;


import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.util.Random;


/**
 * A {@link RandomSnackbar} is basically a {@link Toast} which is only showed to the user with the specified propability.
 *
 * @author Maksl5[Markus Bensing]
 */
public class RandomSnackbar {


    private final CharSequence message;
    private final double probability;
    private final View parentView;

    private RandomSnackbar(View parentView,

                           CharSequence message,
                           double probability) {

        this.parentView = parentView;

        this.message = message;
        this.probability = probability;

    }

    /**
     * Creates a new {@link RandomSnackbar} instance to show.
     *
     * @param parentView  The parent View at which the snackbar will adjust.
     * @param message     The message shown to the user.
     * @param probability The probability the Toast will be shown on calling show().
     * @return The created {@link RandomSnackbar} instance.
     */
    public static RandomSnackbar create(View parentView,
                                        CharSequence message,
                                        double probability) {

        return new RandomSnackbar(parentView, message, probability);
    }

    public void show() {

        if (new Random().nextDouble() <= probability) {

            Snackbar.make(parentView, message, Snackbar.LENGTH_LONG).show();

        }
    }

}
