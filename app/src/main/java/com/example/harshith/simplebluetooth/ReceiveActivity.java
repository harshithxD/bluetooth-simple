package com.example.harshith.simplebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.UUID;

public class ReceiveActivity extends AppCompatActivity {


    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    TextView sensor1,sensor2,sensor3;


    public String[] sarr = new String[3];


    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private StringBuilder stringBuilder = new StringBuilder();
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    Handler bluetoothIn;
    final int handlerState = 0;
    private ConnectedThread connectedThread;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        sarr[0] = "s1v";
        sarr[1] = "s2v";
        sarr[2] = "s3v";

        arrayAdapter = new ArrayAdapter<String>(getBaseContext(),R.layout.device_name);
        listView = (ListView) findViewById(R.id.valuesList);
        listView.setAdapter(arrayAdapter);

//        textView1 = (TextView) findViewById(R.id.text1);
//        textView2 = (TextView) findViewById(R.id.text2);
        sensor1 = (TextView) findViewById(R.id.sensor1);
        sensor2 = (TextView) findViewById(R.id.sensor2);
        sensor3 = (TextView) findViewById(R.id.sensor3);

        bluetoothIn = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == handlerState) {
                    Bundle bundle = (Bundle) msg.obj;
                    int[] sv = new int[3];
                    for(int i = 0;i != 3;i++){
                        sv[i] = bundle.getByte(sarr[i]) & 0xFF;

                    }
                    arrayAdapter.add(String.valueOf(sv[0]));
                    sensor1.setText(String.valueOf(sv[0]));
                    sensor2.setText(String.valueOf(sv[1]));
                    sensor3.setText(String.valueOf(sv[2]));
                }
            }
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createInsecureRfcommSocketToServiceRecord(uuid);
    }




    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            bluetoothSocket = createBluetoothSocket(device);
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(),"Could not create socket",Toast.LENGTH_SHORT).show();
        }

        try {
            bluetoothSocket.connect();
        }
        catch (IOException e) {
            try{
                bluetoothSocket.close();
            }
            catch (IOException e1){
                Toast.makeText(getBaseContext(),"Could not close socket",Toast.LENGTH_SHORT).show();
            }
        }
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
        connectedThread.write("x");

    }




    @Override
    protected void onPause() {
        super.onPause();
        try {
            bluetoothSocket.close();
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(),"Could not close socket while pausing",Toast.LENGTH_SHORT).show();
        }
    }




    private void checkBTState() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(this,"This device doesn't support bluetooth.",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent,1);
            }
        }
    }





    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e){
                Toast.makeText(getBaseContext(),"ERROR - Could not acquire input/output stream from socket",Toast.LENGTH_SHORT).show();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run() {
            byte[] buffer = new byte[32];
            int bytes = -1;
            Bundle bundle = new Bundle();
            while(true){


//                for(int i = 0;i != 3;i++){
                    try {
                        bytes = inputStream.read(buffer);
                        bundle.putByte(sarr[0], buffer[0]);
                        int x = -1;

                        for (int j = 0;j < bytes;j++) {
                            byte by = buffer[j];
                            String s1 = String.format("%8s", Integer.toBinaryString(by & 0xFF)).replace(' ', '0');
                            x = by & 0xFF;
                            char c = (char) x;
                            Log.d("Binary", s1);
                            Log.d("Integer", String.valueOf(x));
                            Log.d("Char", String.valueOf(c));
                        }

                        String readMessage = new String(buffer,0,bytes);
                        bundle.putString(sarr[0],readMessage);
                        Log.d("String : ",readMessage);
                    } catch (IOException e) {
                        break;
                    }
//                }

                bluetoothIn.obtainMessage(handlerState, bytes, -1, bundle).sendToTarget();
            }
        }

        public void write(String input) {
            byte[] buffer = input.getBytes();
            try {
                outputStream.write(buffer);
            }
            catch (IOException e){
                Toast.makeText(getBaseContext(),"Connection Failure",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
