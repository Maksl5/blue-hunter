/**
 *  ErrorHandler.java in com.maksl5.bl_hunt
 *  © Maksl5[Markus Bensing] 2013
 */
package com.maksl5.bl_hunt;



import android.content.Context;

import com.maksl5.bl_hunt.net.Authentification;



/**
 * @author Maksl5[Markus Bensing]
 * 
 */
public class ErrorHandler {

	public static String getErrorString(Context con,
										int requestId,
										int error) {

		String errorMsg;

		switch (requestId) {
		case Authentification.NETRESULT_ID_SERIAL_CHECK:

			errorMsg = con.getString(R.string.str_Error_serialCheck, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1004:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_serialCheck_100_1_2_4));
				break;
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_serialCheck_100_3));
				break;
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_serialCheck_100_5));
				break;
			case 1006:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_serialCheck_100_6));
				break;
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_serialCheck_100_7));
				break;
			}

			return errorMsg;

		case Authentification.NETRESULT_ID_GET_USER_INFO:

			errorMsg = con.getString(R.string.str_Error_getUserInfo, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			}

			return errorMsg;

		case Authentification.NETRESULT_ID_UPDATED:

			String updateMsg = con.getString(R.string.str_auth_updated);

			switch (error) {
			case 1:
			case 4:
			case 5:
				updateMsg += String.format("(%s)", con.getString(R.string.str_auth_updated_1_4_5));
				break;
			case 90:
				updateMsg += String.format("(%s)", con.getString(R.string.str_auth_updated_90));
				break;
			case 404:
				updateMsg += String.format("(%s)", con.getString(R.string.str_auth_updated_404));
				break;
			case 500:
				updateMsg += String.format("(%s)", con.getString(R.string.str_auth_updated_500));
				break;
			}
			return updateMsg;
		case Authentification.NETRESULT_ID_PASS_CHANGE:

			errorMsg = con.getString(R.string.str_Error_changePass, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1003:
			case 1004:
			case 10016:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_1_2_3_4_16));
				break;
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_5));
				break;
			case 1006:
			case 1008:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_6_8));
				break;
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_7));
				break;
			case 1009:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_9));
				break;
			case 1010:
			case 1013:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_10_13));
				break;
			case 1011:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_11));
				break;
			case 1012:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_12));

				break;
			case 1014:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_14));
				break;
			case 1015:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_changePass_100_15));

				break;
			}
			return errorMsg;

		case Authentification.NETRESULT_ID_PRE_LOGIN:

			errorMsg = con.getString(R.string.str_Error_preLogin, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1006:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_preLogin_100_1_2_6));
				break;
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_preLogin_100_3));
				break;
			case 1004:
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_preLogin_100_4_5));
				break;
			}

			return errorMsg;
		case Authentification.NETRESULT_ID_LOGIN:

			errorMsg = con.getString(R.string.str_Error_Login, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1003:
			case 1011:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_Login_100_1_2_3_11));
				break;
			case 1004:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_4));
				break;
			case 1005:
			case 1007:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_5_7));
				break;
			case 1006:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_6));
				break;
			case 1008:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_8));
				break;
			case 1009:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_9));
				break;
			case 1010:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_Login_100_10));
				break;

			}
			return errorMsg;
		case Authentification.NETRESULT_ID_CHECK_LOGIN:

			errorMsg = con.getString(R.string.str_Error_checkLogin, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkLogin_100_1_2));
				break;
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkLogin_100_3));
				break;
			case 1004:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkLogin_100_4));

				break;
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkLogin_100_5));
				break;
			case 1006:
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkLogin_100_6_7));
				break;
			}
			return errorMsg;

		case Authentification.NETRESULT_ID_SYNC_FD_CHECK:

			errorMsg = con.getString(R.string.str_Error_checkSync, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_1_2_3));
				break;
			case 1004:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_4));
				break;
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_5));

				break;
			case 1006:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_6));
				break;
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_7));
				break;
			case 1008:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_8));
				break;
			case 1009:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_checkSync_100_9));
				break;
			}
			return errorMsg;
			
		case Authentification.NETRESULT_ID_SYNC_FD_APPLY:
			
			errorMsg = con.getString(R.string.str_Error_applySync, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_1_2_3));
				break;
			case 1004:
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_4_7));
				break;
			case 1005:
			case 1008:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_5_8));

				break;
			case 1006:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_6));
				break;
			case 1009:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_9));
				break;
			case 1010:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_10));
				break;
			case 1011:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_11));
				break;
			case 1012:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applySync_100_12));
				break;
			}
			return errorMsg;
			
case Authentification.NETRESULT_ID_APPLY_NAME:
			
			errorMsg = con.getString(R.string.str_Error_applyName, error);

			switch (error) {
			case 1:
			case 4:
			case 5:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_1_4_5));
				break;
			case 404:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_404));
				break;
			case 500:
				errorMsg += String.format(" (%s)", con.getString(R.string.str_Error_500));
				break;
			case 1001:
			case 1002:
			case 1003:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_1_2_3));
				break;
			case 1004:
			case 1006:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_4_6));
				break;
			case 1005:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_5));

				break;
			case 1007:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_7));
				break;
			case 1008:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_8));
				break;
			case 109:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_9));
				break;
			case 1010:
				errorMsg +=
						String.format(" (%s)", con.getString(R.string.str_Error_applyName_100_10));
				break;
			}
			return errorMsg;
			
		default:
			return "No error description found.";
		}

	}

}
