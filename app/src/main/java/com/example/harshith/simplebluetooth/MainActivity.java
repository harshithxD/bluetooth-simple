package com.example.harshith.simplebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static String EXTRA_DEVICE_ADDRESS;
    TextView textConnectionStatus;
    ListView pairedListView;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String>  mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textConnectionStatus = (TextView) findViewById(R.id.connecting);
        textConnectionStatus.setTextSize(40);
        textConnectionStatus.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.device_name);


        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkBTState();

        mPairedDevicesArrayAdapter.clear();

        textConnectionStatus.setText(" ");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for(BluetoothDevice bluetoothDevice : pairedDevices) {
                mPairedDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

            }
        }
        else {
            mPairedDevicesArrayAdapter.add("No devices paired");
        }



    }

    private void checkBTState() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(this,"This device doesn't support bluetooth.",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if(!mBluetoothAdapter.isEnabled()){
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent,1);
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            textConnectionStatus.setText("Connecting...");

            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(MainActivity.this,ReceiveActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS,address);
            startActivity(i);
        }
    };
}
