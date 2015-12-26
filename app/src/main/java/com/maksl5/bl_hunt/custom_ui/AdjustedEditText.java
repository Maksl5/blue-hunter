
package com.maksl5.bl_hunt.custom_ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;



/**
 * @author Maksl5
 * 
 */
public class AdjustedEditText extends EditText {

	private final List<OnBackKeyClickedListener> listeners = new ArrayList<>();

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AdjustedEditText(Context context,
			AttributeSet attrs,
			int defStyle) {

		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AdjustedEditText(Context context,
			AttributeSet attrs) {

		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public AdjustedEditText(Context context) {

		super(context);

	}

	@Override
	public boolean onKeyPreIme(	int keyCode,
								KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

			fireOnBackKeyClicked();

		}
		return super.onKeyPreIme(keyCode, event);
	}

	public void setOnBackKeyClickListener(OnBackKeyClickedListener onBackKeyPressedListener) {

		listeners.add(onBackKeyPressedListener);
	}

	private void fireOnBackKeyClicked() {

		for (OnBackKeyClickedListener listener : listeners) {
			listener.onBackKeyClicked();
		}
	}

	public interface OnBackKeyClickedListener {

		void onBackKeyClicked();
	}

}