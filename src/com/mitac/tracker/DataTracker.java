package com.mitac.tracker;





import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;

public class DataTracker extends Activity {
	/** Called when the activity is first created. */
	private static final String TAG = "DataTracker";
//    public static final String ACTION = "com.mitac.tracker.DataTrackerService";
	public static boolean m_log = true;
	private boolean m_start_service = false;

	Button startBtn;
	Button stopBtn;

	ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "####################### onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "####################### onServiceDisconnected");
        }
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    if(m_start_service == false) {
                    Log.v(TAG, "##################################start logging");
			        m_start_service = true;
    			    Intent mIntent = new Intent();
    			    mIntent.setAction("com.mitac.tracker.DataTrackerService");
    				bindService(mIntent, conn, BIND_AUTO_CREATE);
                    startService(mIntent);
			    }
			}
		});

        Log.v(TAG, "##################################2222222222");
        stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
                if(m_log == false) {
                    Log.v(TAG, "##################################start logging");
                   m_log = true;
                    stopBtn.setText(R.string.stop);
                } else {
                    Log.v(TAG, "##################################stop logging");
                    m_log = false;
                    stopBtn.setText(R.string.start);
                }
			}
		});

	}


	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy unbindService");
		unbindService(conn);
		super.onDestroy();
	};
}