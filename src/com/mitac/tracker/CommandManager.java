package com.mitac.tracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class CommandManager {
    private static final String TAG = "DataTracker";

    public static synchronized String run_command(String[] cmd, String workdirectory) {
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            InputStream in = null;
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.d(TAG, "Exception: " + ex);
        }
        return result.toString();
    }

    public static synchronized String run_command2(String command)
    {
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while (null != (line = reader.readLine()))
            {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            is.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
        return null;
    }

    public static synchronized String run_command3(String command)
    {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            int status = process.waitFor();
            if (status == 0) {
                Log.d(TAG, "Success\n");
            } else {
                Log.d(TAG, "Failed\n");
            }
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder strBuild = new StringBuilder();
            String line = new String();
            while((line=buf.readLine()) != null){
                line = line + "\n";
                strBuild.append(line);
            }
            buf.close();
            //Log.d(TAG, "******* "+strBuild.toString());
            return strBuild.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
        return null;
    }

/*FIXME*/
/*
    public static synchronized String run_command4(String command)
    {
        Process p = null;
        try {
            p = new ProcessBuilder("sh").redirectErrorStream(true).start();

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command);
            os.flush();

            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();

            // read ping replys
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line+"\n");
            }
            reader.close();
            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != p) {
                p.destroy();
            }
        }
        return null;
    }
*/

}
