package com.heartbeat.watafuru.heartbeat;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class Settings extends ListActivity {

    //test for new branch

    private static final String TAG = "";
    ArrayAdapter mArrayAdapter;
    ArrayAdapter mNameAdapter;
    BluetoothAdapter mBluetoothAdapter;
    UUID mUUID;
    BluetoothSocket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        mNameAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        mUUID = UUID.randomUUID();

        if (mBluetoothAdapter == null) {
            //TODO: add a "I need bluetooth message"

        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 8;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                bluetoothIsOn();
            }
            setListAdapter(mNameAdapter);

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //TODO ADD CONECT AS CLIENT
        BluetoothDevice bd = (BluetoothDevice) mArrayAdapter.getItem(position);
        Log.i(TAG,bd.getName());
        connectAsClient(bd);
    }

    private void connectAsClient(BluetoothDevice bd) {
        try {
            BluetoothSocket mSocket = bd.createRfcommSocketToServiceRecord(mUUID);
        } catch (IOException e) {
            Toast.makeText(Settings.this,"Something went wrong. Try again!",Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    private void discoverBluetooth() {
        mBluetoothAdapter.startDiscovery();

        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //Take the device from the intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //add name and adress to the adapter
                    mNameAdapter.add(device.getName() + "\t" + device.getAddress());
                    mArrayAdapter.add(device);
                }
            }
        };


        //Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); //TODO: unregister during on destroy

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, 300);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 8) {
            if (resultCode == RESULT_OK) {
                bluetoothIsOn();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(Settings.this, "UGH! Something went wrong :(!",Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 300) {
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(Settings.this,"Something went wrong, please try again!",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Settings.this, "You're discoverable!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bluetoothIsOn() {
        //Called when bluetooth is turned on

        Toast.makeText(Settings.this, "Bluetooth is on!", Toast.LENGTH_SHORT).show();
        listPairedDevices();
        discoverBluetooth();
    }

    private void listPairedDevices() {
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
        Log.i(TAG, paired.toString());
        if (paired.size() > 0) {
            for (BluetoothDevice b : paired) {

                mNameAdapter.add(b.getName() + "\t" + b.getAddress());
                mArrayAdapter.add(b);
                Log.i(TAG, b.getName() + "\t" + b.getAddress());
            }
        }
    }
}
