package com.mitac.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
//import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Environment;
//import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
//import android.widget.CheckBox;
//import android.widget.Toast;
//import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

/**
 * Android Service
 * 
 * @author dev
 * 
 */
public class DataTrackerService extends Service {
    private static final String TAG = "DataTrackerService";

    private TelephonyManager mTelephonyManager;
    SignalStrength mSignalStrength = null;
    private ServiceState mServiceState;
//    private int radio35G = 0;
    private boolean mStorage = false;
    private static String strAppPath = "/data/data/com.mitac.tracker/files/";
    private String m_strName = "";// TAG+".csv";
    private static int SAMPLE_INTERVAL = 5000;
    private String m_strPing = "Ping FAIL";

    /**
     * SaveToFile:  /data/data/com.mitac.tracker/files/
     * Gaia SD path: /storage/emulated/0
     * Ulmo: /mnt/sdcard
     */
    private void SaveToFile(String content) {
        try {
            FileOutputStream outputStream = openFileOutput(m_strName, Activity.MODE_APPEND);
            if(outputStream != null) {
              outputStream.write(content.getBytes());
              outputStream.flush();
              outputStream.close();
            }
            //Toast.makeText(DataTrackerService.this, "record signal strength into the file", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Handler dataHandler = new Handler();
    private Runnable retryRunnable = new Runnable() {
        public void run() {
//            Log.i(TAG, "*****************************");
            if(DataTracker.m_log == true) {
                   if (m_strName.compareTo("") == 0) {
                        long time = System.currentTimeMillis();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MMdd_HHmm-ss");
                        String newtime = formatter.format(new Date(time));
                        m_strName = newtime;
                        m_strName = m_strName + ".csv";
                        Log.i(TAG, "########################### "+m_strName);
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    long time = System.currentTimeMillis();
                    Date d1 = new Date(time);
                    String strTime = format.format(d1);
                    //Recording the status of network
                    ping();
        
                    String str = null;
                    if(mTelephonyManager!=null && mSignalStrength!=null) {
                        str = strTime+","+mTelephonyManager.getNetworkType() + "," + mSignalStrength.getGsmSignalStrength() +","+m_strPing+"\n";
                    } else {
                        str = strTime+","+"mocked data" + "," + "99" +","+m_strPing+"\n";
                    }
        
                    Log.i(TAG, str);
        
                    SaveToFile(str);

                    if (mStorage) {
                        CopyFileToSD(m_strName);
                    }
            } else {
                    if (m_strName.compareTo("") != 0) {
                        m_strName = "";
                    }
            }
            dataHandler.postDelayed(retryRunnable, SAMPLE_INTERVAL);
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength;
            //updateSignalStrength();
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
            mServiceState = state;
        }

        @Override
        public void onDataConnectionStateChanged(int state) {
            updateDataState();
            // updateNetworkType();
        }
    };

    private String avgSpeed(String str) {
        int position = str.indexOf("min/avg/max");
        //Log.d(TAG, "str: "+str);
        //Log.d(TAG, "position: "+position);
        if (position != -1) {
            String subStr = str.substring(position + 18);
            position = subStr.indexOf("/");
            subStr = subStr.substring(position + 1);
            position = subStr.indexOf("/");
            return subStr.substring(0, position);
        } else {
            return null;
        }
    }

    private boolean ping() {
        String ping_result;
        String PING = "/system/bin/ping -w 4 "+DataTracker.m_strDomain;
        Log.d(TAG, PING);
        //String PING = "/system/bin/cat /sys/sys_info/hw_ver";
        String result = null;
        ping_result = CommandManager.run_command2(PING);

        if (avgSpeed(ping_result) != null) {
            m_strPing = "Ping OK";
            //Log.d(TAG, m_strPing);
            return true;
        } else {
            m_strPing = "Ping FAIL";
            //Log.d(TAG, m_strPing);
            return false;
        }
    }
    private boolean hasService() {
        if (mServiceState != null) {
            switch (mServiceState.getState()) {
            case ServiceState.STATE_OUT_OF_SERVICE:
            case ServiceState.STATE_POWER_OFF:
                return false;
            default:
                return true;
            }
        } else {
            return false;
        }
    }

//    private final void updateSignalStrength() {
//        // Without PhoneServiceState(in STATE_OUT_OF_SERVICE and
//        // STATE_POWER_OFF),
//        // always disable the signal icon
//    }

    private void updateDataState() {
        int state = mTelephonyManager.getDataState();

        switch (state) {
        case TelephonyManager.DATA_CONNECTED:
            Log.e(TAG, "############# DATA_CONNECTED");
            //dataHandler.postDelayed(retryRunnable, SAMPLE_INTERVAL);
            break;
        case TelephonyManager.DATA_SUSPENDED:
            Log.e(TAG, "############# DATA_SUSPENDED");
            break;
        case TelephonyManager.DATA_CONNECTING:
            Log.e(TAG, "############# DATA_CONNECTING");
            break;
        case TelephonyManager.DATA_DISCONNECTED:
            Log.e(TAG, "############# DATA_DISCONNECTED");
            //dataHandler.removeCallbacks(retryRunnable);
            // radio35G = Settings.System.getInt(getContentResolver(), Settings.System.RADIO_35G_ON, 0);
            break;
        }

    }

//    private void setModule35GOn(boolean enabling) {
//
//        // Change the system setting
//        //Settings.System.putInt(getContentResolver(), Settings.System.RADIO_35G_ON, enabling ? 1 : 0);
//
//        //Log.e(TAG, "[RADIO_35G_ON]= " + Settings.System .getInt(getContentResolver(), Settings.System.RADIO_35G_ON, 0));
//
//        // Send the intent to PhoneApp
//        //Intent intent = new Intent(Intent.ACTION_35G_STATUS_CHANGED);
//        //intent.putExtra("state", enabling);
//        //sendBroadcast(intent);
//    }

    @Override
    public void onCreate() {
        Log.i(TAG, "DataTrackerService onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "DataTrackerService onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "DataTrackerService  onStartCommand");
        return super.onStartCommand(intent, flags, startId);
   }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "DataTrackerService onStart");
        super.onStart(intent, startId);
        
        Log.i(TAG, "########################### 1");
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.i(TAG, "########################### 1111111111");
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                                     | PhoneStateListener.LISTEN_SERVICE_STATE
                                                     | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        
        Log.i(TAG, "########################### 2");
        if (m_strName.compareTo("") == 0) {
            long time = System.currentTimeMillis();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MMdd_HHmm-ss");
            String newtime = formatter.format(new Date(time));
            m_strName = newtime;
            m_strName = m_strName + ".csv";
            Log.i(TAG, "########################### "+m_strName);
       }
        Log.i(TAG, "########################### 3");
        dataHandler.postDelayed(retryRunnable, SAMPLE_INTERVAL);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "DataTrackerService onDestroy");
        dataHandler.removeCallbacks(retryRunnable);
        if (m_strName.compareTo("") != 0) {
             m_strName = "";
        }
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "DataTrackerService onUnbind");

        dataHandler.removeCallbacks(retryRunnable);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        return super.onUnbind(intent);
    }

    public static boolean CopyFileToSD(String srcFile) {

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            File file = new File(strAppPath + srcFile);
            if (!file.exists())
                return false;

            File sdCardDir = Environment.getExternalStorageDirectory();
            File sdFolder = new File(sdCardDir, TAG);
            if (!sdFolder.exists()) {
                sdFolder.mkdirs();
            }
            File sdFile = new File(sdFolder, srcFile);

            Log.i(TAG, "SRC: " + file.getPath());
            Log.i(TAG, "DST: " + sdFile.getPath());

            if (file.exists()) {
                int byteread = 0;
                InputStream in = null;
                OutputStream out = null;

                try {
                    in = new FileInputStream(file);
                    out = new FileOutputStream(sdFile);
                    byte[] buffer = new byte[1024];

                    while ((byteread = in.read(buffer)) != -1) {
                        out.write(buffer, 0, byteread);
                    }
                    return true;
                } catch (FileNotFoundException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                } finally {
                    try {
                        if (out != null)
                            out.close();
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
    
}
