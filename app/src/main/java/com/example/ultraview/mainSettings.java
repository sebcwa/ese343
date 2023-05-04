package com.example.ultraview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class mainSettings extends AppCompatActivity {
    private int spf;
    private BluetoothGatt mGatt;
    private String skinTone;
    private static final UUID SERVICE_UUID = UUID.fromString("4FAFC201-1FB5-459E-8FCC-C5C9C331914B");
    private static final UUID BLEChar = UUID.fromString("CA73B3BA-39F6-4AB3-91AE-186DC9577D99");
    private TextView batval;
    private Button temp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       // batval = findViewById(R.id.battvalue);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("F4:12:FA:4F:A5:71");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mGatt = device.connectGatt(this, false, gattCallback);

        EditText spfEditText = findViewById(R.id.spfEditText);
        String spfString = spfEditText.getText().toString();


        if (!spfString.isEmpty()) {
            spf = Integer.parseInt(spfString);
        }



        int finalSpf = spf;
        spfEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // update the shared preference value here
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Log.d("SPF:", Integer.toString(finalSpf));
                editor.putInt("spf", Integer.parseInt(s.toString()));
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
                Toast.makeText(getApplicationContext(), "Spf Set!", Toast.LENGTH_SHORT).show();
            }

        });

        GridLayout gridLayout = findViewById(R.id.grid_layout);

        // Define colors for each button
        int[] buttonColors = { 0xFFFFF0D7, 0xFFF7C99C, 0xFFEDAE6B, 0xFFD69C6F,
                0xFFB46A46, 0xFF8C5543, 0xFF573D29, 0xFF40241A,0xFF27170F};

        // Create buttons and add them to the grid layout
        setGridLayout();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("settings -", "Connected to GATT server.");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("settings -", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLEChar);
                    if (characteristic != null ) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                }
            } else {
                Log.d("setting", "Service discovery failed.");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("characterisitc", String.valueOf(characteristic));
            byte[] values = characteristic.getValue();
            Log.d("array", String.valueOf(values));
            Log.d("Byte Array", String.valueOf((char)values[0]+(char)values[1]+(char)values[2] +(char)values[3] + (char)values[4] + (char)values[5]));
            String UV = String.valueOf((char)values[2] + (char)values[4] + (char)values[5]);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    batval.setText(UV);
                }
            });
        }
    };

    private void setGridLayout(){
        final Button[] previousButton = {null}; // variable to store reference to previously clicked button

        GridLayout gridLayout = findViewById(R.id.grid_layout);
        int[] buttonColors = { 0xFFFFF0D7, 0xFFF7C99C, 0xFFEDAE6B, 0xFFD69C6F,
                0xFFB46A46, 0xFF8C5543, 0xFF573D29, 0xFF40241A,0xFF27170F};

// Create buttons and add them to the grid layout
        for (int i = 0; i < 9; i++) {
            Button button = new Button(this);
            button.setBackgroundColor(buttonColors[i]);
            button.setLayoutParams(new ViewGroup.LayoutParams(GridLayout.LayoutParams.WRAP_CONTENT, GridLayout.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Skin Tone Selected", Toast.LENGTH_SHORT).show();
                    Log.d("button", String.valueOf(button.getBackground()));

                    // reset the background color of the previously clicked button
                    if (previousButton[0] != null) {
                        previousButton[0].setBackgroundColor(buttonColors[gridLayout.indexOfChild(previousButton[0])]);
                    }

                    // set the background color of the current button to white
                    button.setBackgroundColor(Color.WHITE);
                    previousButton[0] = button;
                    // setGridLayout();
                }
            });

            gridLayout.addView(button);
        }

    }



}