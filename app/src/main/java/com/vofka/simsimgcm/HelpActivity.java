package com.vofka.simsimgcm;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

public class HelpActivity extends Activity {

    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "HelpActivity");
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        amKillProcess("com.vofka.simsimgcm.mainactivity");

        //     setContentView(findViewById(R.id.sim_help_1));
        // Set the layout for this activity.  You can find it
        // in res/layout/help.xml
        //  setContentView(help);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //       getMenuInflater().inflate(R.menu.main, menu);
        //      return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }



/////

    /*   @Override
       public boolean onOptionsItemSelected(MenuItem item) {
           // Handle action bar item clicks here. The action bar will
           // automatically handle clicks on the Home/Up button, so long
           // as you specify a parent activity in AndroidManifest.xml.
           int id = item.getItemId();
           //    if (id == R.id.action_settings) {
           //         return true;
           //     }

        if(id == R.id.settings) {

            //     Log.i(LOG_TAG, "settings");
            Intent prefs = new Intent(this, Preferences.class);
            startActivity(prefs);
            amKillProcess("com.vofka.simsim");
            return true;
            }

           if (id == R.id.exit) {
               if (mWakeLock.isHeld()) {
                   mWakeLock.release();
               }
               //       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
               //    amKillProcess("com.vofka.simsimeye");
               amKillProcess("com.vofka.simsim");
               return true;
           }
           return super.onOptionsItemSelected(item);
       }

    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settingsHelp:
                //   Log.i(LOG_TAG, "settings");



                Intent intent;
                intent=new Intent(this,Preferences.class);
                startActivity(intent);


          //      Intent prefs = new Intent(this, Preferences.class);
         //       startActivity(prefs);
                break;

            case R.id.exit:
                amKillProcess("com.vofka.simsimgcm");
                break;
            //    return true;

        }
        return super.onOptionsItemSelected(item);
        //   return true;
    }



    public void amKillProcess(String process)
    {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses)
        {
            if(runningProcess.processName.equals(process))
            {
                android.os.Process.sendSignal(runningProcess.pid, android.os.Process.SIGNAL_KILL);
            }
        }
    }
}