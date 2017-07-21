package com.mitac.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "DataTrackerService";

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		Log.e(TAG, "BOOT_COMPLETED");
		Intent mBootIntent = new Intent(arg0, DataTrackerService.class);
		arg0.startService(mBootIntent);
	}
}
