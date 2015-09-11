package com.vofka.simsimgcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class MainActivity extends Activity  {
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private Handler mHandler = new Handler();
    public static int SEARCH_INTERVAL =1*40000;
    private static final long WAIT_ON_BLUETOOTH_INTERVAl=5;  //5
    public String  firstPairedDevice="";
    public boolean bletoothIsOn=false;
    BluetoothAdapter bluetoothAdapter = null;

    ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

    public Runnable searchBluetoothDevicesTask = new Runnable() {
        public void run() {

            bluetoothAdapter = null;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            bletoothIsOn=false;
            firstPairedDevice="";

            offBluetooth();
            onBluetooth();
            if (bletoothIsOn){
                getPairedDevices();
            }else {               // second try
                offBluetooth();
                onBluetooth();
                if (bletoothIsOn) {
                    getPairedDevices();
                }
            }
            mHandler.postDelayed(searchBluetoothDevicesTask,SEARCH_INTERVAL);
        }
    };
 //   Future searchBluetoothDevicesTaskFuture = threadPoolExecutor.submit(searchBluetoothDevicesTask);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //      searchBluetoothDevicesTask.run();
            searchBluetoothDevicesOnce();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
     //   searchBluetoothDevicesTask.run();
    }

    public void searchBluetoothDevicesOnce() {
        bluetoothAdapter = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bletoothIsOn=false;
        firstPairedDevice="";
        offBluetooth();
        onBluetooth();
        if (bletoothIsOn){
            getPairedDevices();
        }else {               // second try
            offBluetooth();
            onBluetooth();
            if (bletoothIsOn) {
                getPairedDevices();
            }
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
            startBluetoothAFR();

        }else{
        //    SEARCH_INTERVAL=SEARCH_INTERVAL*20;
            mHandler.removeCallbacksAndMessages(searchBluetoothDevicesTask);
     //       searchBluetoothDevicesTaskFuture.cancel(true);
            intentHelpActivity();

            //  setContentView(R.layout.help);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.exit) {
            amKillProcess("com.vofka.simsimgcm");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  void startBluetoothAFR() {
        Intent intentBtAFR;

        intentBtAFR = new Intent(this,Bluetooth2.class);
        intentBtAFR.putExtra("message",firstPairedDevice);
        startActivity(intentBtAFR);
        finish();
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
    public void intentHelpActivity(){
        Intent intent;
      intent=new Intent(this,HelpActivity.class);
   //   intent=new Intent(this,Preferences.class);
        startActivity(intent);
        finish();
    }

    class HandleSeacrh extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:

                    break;

                default:
                    break;
            }
        }
    }
}
