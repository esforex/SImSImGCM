package com.vofka.simsimgcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"ALL", "StringEquality"})
public class NoiseAlert extends Activity {
    /* constants */
    private static final int WAIT_NOISE_ACTIVITY_INTERVAL =35000;  //10 sec  hitCount=0
    private static final String LOG_TAG = "NoiseAlert";
    private static final int POLL_INTERVAL = 300;
    private static final int NO_NUM_DIALOG_ID=1;
    private static final String[] REMOTE_CMDS = {"start", "stop", "panic"};
    private static final long WAIT_ON_BLUETOOTH_INTERVAl=5;

    /** running state **/
    private boolean mAutoResume = false;
    private boolean mRunning = false;
    private boolean mTestMode = false;
    private int mTickCount = 0;
    private int mHitCount =0;

    /** config state **/
    private int mThreshold;
    private int mPollDelay;
    private int mHints=100;
    private String mPhoneNumber;
    // private String mPhoneNumber_user;

    private String mSmsSecurityCode;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView;
    private ImageView mActivityLed;
    private SoundLevelView mDisplay;

    /* data source */
    private SoundMeter mSensor;

    /* SMS remote control */
    private SmsRemote mRemote;

    BluetoothAdapter bluetoothAdapter = null;
    public String  firstPairedDevice="";
    public boolean bletoothIsOn = false;
    public String message="";
    public static int SEARCH_INTERVAL =1*40000;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    public boolean foundPaired=false;
    public  String devAddress="";
    public  String devName="";


    private Runnable mSleepTask = new Runnable() {
        public void run() {
            try {
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            if (mTestMode) updateDisplay("testing...", amp);
            else           updateDisplay("listening...", amp);

            if ((amp > mThreshold) && !mTestMode) {
                mHitCount++;
                if (mHitCount > mHints){     /////   ALERT IS HERE
                    intentCamera();
             //       callForHelp();     //   call to master phone
                    return;
                }
            }

            mTickCount++;
            setActivityLed(mTickCount% 2 == 0);

            if ((mTestMode || mPollDelay > 0) && mTickCount > 100) {
                if (mTestMode) {
                    stop();
                } else {
                    sleep();
                }
            } else {
                mHandler.postDelayed(mPollTask, POLL_INTERVAL);
            }
        }
    };

    public Runnable searchBluetoothDevicesTask = new Runnable() {
        public void run() {
            getPairedDevicesMessage();
               mHandler.postDelayed(searchBluetoothDevicesTask,SEARCH_INTERVAL);
        }
    };

    public  Boolean noiseActivityFinished=false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
///
        //    if commented  follow user choice
        //     mPhoneNumber_user="87017331397";   //  my
        //    mPhoneNumber_user="89831716269";   //  fintis


        //////////////////////////

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
     //   readApplicationPreferences();

        //////////////////////////////

        setContentView(R.layout.main);
        mStatusView = (TextView) findViewById(R.id.status);
        mActivityLed = (ImageView) findViewById(R.id.activity_led);

        mSensor = new SoundMeter();
        mDisplay = (SoundLevelView) findViewById(R.id.volume);
        mRemote = new SmsRemote();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new PostNoiseActivityTask().execute();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        searchBluetoothDevicesTask.run();
    }

    @Override
    public void onResume() {
        super.onResume();
        readApplicationPreferences();
        if (mSmsSecurityCode.length() != 0) {
            mRemote.register(this, mSmsSecurityCode, REMOTE_CMDS);
        }
        mDisplay.setLevel(0, mThreshold);
        if (mAutoResume) {
            try {
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //     noiseActivityFinished=true;
        onStop();
    }

    @Override
    public void onStop() {
        super.onStop();

        mRemote.deregister();
        stop();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemote.deregister();
        //   Toast.makeText(getApplicationContext(), "SimSimNoise. onDestroy()", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //     menu.findItem(R.id.test).setEnabled(!mRunning);
        if (mRunning) {
            menu.findItem(R.id.start_stop).setTitle(R.string.stop);
        } else {
            menu.findItem(R.id.start_stop).setTitle(R.string.start);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settingsNoise:
                Log.i(LOG_TAG, "settings");
                Intent prefs = new Intent(this, com.vofka.simsimgcm.Preferences.class);
                startActivity(prefs);
                break;
            case R.id.start_stop:
                if (!mRunning) {

                    if (mPhoneNumber.length() == 0) {
                        showDialog(NO_NUM_DIALOG_ID);
                        break;
                    }
                    mAutoResume = true;
                    mRunning = true;
                    mTestMode = false;
                    try {
                        start();

                        //
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mAutoResume = false;
                    mRunning = false;
                    stop();
                }
                break;
            //      case R.id.test:
            //           mTestMode = true;
            //           try {
            //              start();``
            //           } catch (IOException e) {
            //               e.printStackTrace();
            //            }
            //            break;

            case R.id.panic:
                callForHelp();
                break;
            case R.id.exit:
                amKillProcess("com.vofka.simsim");
                break;
            //    return true;
            case R.id.help:
                Intent myIntent = new Intent();
                myIntent.setClass(this, com.vofka.simsimgcm.HelpActivity.class);
                startActivity(myIntent);
                break;
            //      setContentView(findViewById(R.id.sim_help_1));
            //     ImageView img = new ImageView(this);  // or (ImageView) findViewById(R.id.myImageView);
            //     img.setImageResource(R.drawable.sim_help);
        }
        return true;
    }

    public void receive(String cmd) {
        if (cmd == "start" & !mRunning) {
            if (mPhoneNumber.length() != 0  ) {
                mAutoResume = true;
                mRunning = true;
                mTestMode = false;
                try {
                    start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (cmd == "stop" & mRunning) {
            mAutoResume = false;
            mRunning = false;
            stop();
        } else if (cmd == "panic") {
            callForHelp();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == NO_NUM_DIALOG_ID) {
            return new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon)
                    .setTitle(R.string.no_num_title)
                    .setMessage(R.string.no_num_msg)
                    .setNeutralButton(R.string.ok, null)
                    .create();
        }
        else return null;
    }

    private void start() throws IOException {
        mTickCount = 0;
        mHitCount = 0;
        try {
            mSensor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setActivityLed(true);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
////////////////////////////////////////////////////   here I stop main cycle
        //      amKillProcess("com.vofka.simsim.mainactivity");
////////////////////////////////////////////////////

        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        mDisplay.setLevel(0,0);
        updateDisplay("stopped...", 0.0);
        setActivityLed(false);
        mRunning = false;
        mTestMode = false;
    }

    private void sleep() {
        mSensor.stop();
        updateDisplay("paused...", 0.0);
        setActivityLed(false);
        mHandler.postDelayed(mSleepTask, 1000*mPollDelay);
    }

    private void readApplicationPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPhoneNumber = prefs.getString("alert_phone_number", null);
        //       Log.i(LOG_TAG, "phone number = " + mPhoneNumber);
        mThreshold = Integer.parseInt(prefs.getString("threshold", null));
        //      Log.i(LOG_TAG, "threshold=" + mThreshold);
        mHints = Integer.parseInt(prefs.getString("hints", null));
   //              Log.i(LOG_TAG, "hints=" + mHints);

        mPollDelay = Integer.parseInt(prefs.getString("sleep", null));
        //       Log.i(LOG_TAG, "sleep=" + mPollDelay);
        mSmsSecurityCode = prefs.getString("sms_security_code", null);
    }

    private void updateDisplay(String status, double signalEMA) {
        mStatusView.setText(status);
        mDisplay.setLevel((int) signalEMA, mThreshold);
    }

    private void setActivityLed(boolean on) {
        mActivityLed.setVisibility( on ? View.VISIBLE : View.INVISIBLE);
    }

    public void callForHelp() {
        if (mPhoneNumber.length() == 0) {
            stop();
            showDialog(NO_NUM_DIALOG_ID);
            return;
        }
        mAutoResume = false;
        stop();
        final Uri number = Uri.fromParts("tel", mPhoneNumber, "");
        startActivity(new Intent(Intent.ACTION_CALL, number));

        String custom_num="";
        //   custom_num="87017331397";   Utkin
        //    custom_num="87075777187";  //  zinga35789@gmail.com

        final Uri nu=Uri.fromParts("tel",custom_num,"");
        //    startActivity(new Intent(Intent.ACTION_CALL, nu));

        mAutoResume=true;
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private class PostNoiseActivityTask extends AsyncTask<Boolean, Integer, Boolean> {
        protected boolean doInBackground(Boolean noiseActivityFinished) {
            return noiseActivityFinished;
        }

        protected void onProgressUpdate(Integer... progress) {
            setProgressPercent(progress[0]);
        }

        private void setProgressPercent(Integer progres) {
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {

            Thread thread=new Thread();
            try {
                thread.sleep(WAIT_NOISE_ACTIVITY_INTERVAL);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return noiseActivityFinished;
        }

        protected void onPostExecute(Boolean foundPaired)
        {
            // reset mHitCount to 0
            mHitCount=0;
            if(!noiseActivityFinished) {
                //      intentTest_BAFR();
                //   intentMainActivity();
                //   finish();
            }
        }
    }



    public void intentCamera() {
        Intent intentCam;
        intentCam = new Intent(this,CameraActivity.class);
        startActivity(intentCam);
        finish();
    }



    public void intentMainActivity() {
        Intent intentMain;
        intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
        finish();
    }
    public void offBluetooth() {
        if(bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.disable();

            while (bluetoothAdapter.isEnabled()) {

                Thread threadOff=new Thread();
                try {
                    threadOff.sleep(WAIT_ON_BLUETOOTH_INTERVAl);
                    if(bluetoothAdapter.isEnabled())
                        bluetoothAdapter.disable();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //     bluetoothAdapter.disable();
                //       Log.i("Log", "Bluetooth is Disablng");

            }
            //        Log.i("Log", "Bluetooth is Disabled");
        }
    }
    public void onBluetooth() {
        if(!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();

            while (!bluetoothAdapter.isEnabled()) {

                Thread threadOn=new Thread();
                try {
                    threadOn.sleep(WAIT_ON_BLUETOOTH_INTERVAl);
                    if(!bluetoothAdapter.isEnabled())
                        bluetoothAdapter.enable();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //     bluetoothAdapter.enable();
                //       Log.i("Log", "Bluetooth is Enablng");

            }
            if (bluetoothAdapter.isEnabled()){
                bletoothIsOn=true;}else {bletoothIsOn=false;}
            //                 Log.i("Log", "Bluetooth is Enabled");
        }
    }
    public void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size()==1)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                String firstBondedDevice=new String();
                firstBondedDevice=device.getAddress();
                firstPairedDevice=firstBondedDevice;
            }
            message=firstPairedDevice;

        }else{
            //  setContentView(R.layout.help);
        }
    }
    public void  getPairedDevicesMessage(){
        bluetoothAdapter = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bletoothIsOn=false;
        firstPairedDevice="";

        offBluetooth();
        onBluetooth();
        if (bletoothIsOn){
            getPairedDevicesGetMessage();
        }else {               // second try
            offBluetooth();
            onBluetooth();
            if (bletoothIsOn) {
                getPairedDevicesGetMessage();
            }
        }
    }

    public void getPairedDevicesGetMessage(){
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size()==1)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                String firstBondedDevice=new String();
                firstBondedDevice=device.getAddress();
                firstPairedDevice=firstBondedDevice;

            }
            message=firstPairedDevice;
            Toast.makeText(getApplicationContext(), "Found Paired 1- " + message, Toast.LENGTH_SHORT).show();
            arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
            startSearching();


        }else{
            intentHelpActivity();
        }
    }
    public void intentHelpActivity(){
        Intent intent;
        intent=new Intent(this,HelpActivity.class);
        //   intent=new Intent(this,Preferences.class);
        startActivity(intent);
        finish();
    }
    public void startSearching() {
        arrayListBluetoothDevices.clear();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //       intentFilter.setPriority(6);
        NoiseAlert.this.registerReceiver(mReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {  ///  private
            Message msg = Message.obtain();
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                //      Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(arrayListBluetoothDevices.size()<1) // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the arraylist.
                    arrayListBluetoothDevices.add(device);
                    devAddress=device.getAddress();
                    devName=device.getName();
                    if( devAddress.equals(message)) {
                        foundPaired=true;
                        Toast.makeText(context, devName, Toast.LENGTH_SHORT).show();
                        compareDevices(context);
                    }
                }
                else
                {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for(int i = 1; i<arrayListBluetoothDevices.size();i++)
                    {
                        if(device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress()))
                        //  here write code for pass aiens devices
                        {
                            devAddress=device.getAddress();
                            devName=device.getName();
                            if(device.getAddress().equals(message)) {
                                flag = true;
                                foundPaired=true;
                                Toast.makeText(context, devName, Toast.LENGTH_SHORT).show();
                                compareDevices(context);
                            }else {
                                flag=false;
                            }
                            //             Log.i("AdressExtra", devAddress);
                        }
                    }
                    if(flag == true)
                    {
                        arrayListBluetoothDevices.add(device);
                        devAddress=device.getAddress();
                        devName=device.getName();
                        //        Log.i("AdressExtraExtra",devAddress );
                        if( devAddress.equals(message)) {
                            foundPaired=true;
                            Toast.makeText(context, devName, Toast.LENGTH_SHORT).show();
                            compareDevices(context);
                        }
                        //       Log.i("MesAdress",message );
                    }
                }
            }
            //   compareDevices();
        }
    };
    public void compareDevices(Context context) {
        if (foundPaired) {
            if (mReceiver != null) {
                NoiseAlert.this.unregisterReceiver(mReceiver);
                bluetoothAdapter.cancelDiscovery();
                mReceiver= null;
            }

                intentMainActivity();  //  runnable
        }else{
            if (mReceiver != null) {
                NoiseAlert.this.unregisterReceiver(mReceiver);
                bluetoothAdapter.cancelDiscovery();
            }
            Toast.makeText(context, "  keep listening..", Toast.LENGTH_SHORT).show();
            // keep listening
        }
    }
}

