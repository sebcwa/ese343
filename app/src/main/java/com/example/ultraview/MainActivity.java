package com.example.ultraview;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.provider.Settings;
import android.location.Location;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLETest";
    private static final UUID SERVICE_UUID = UUID.fromString("4FAFC201-1FB5-459E-8FCC-C5C9C331914B"); // Replace XXXX with the service UUID of your TinyS3 device
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("CBA1D466-344C-4BE3-AB3F-189F80DD7518"); // Replace YYYY with the characteristic UUID of your TinyS3 device
    private static final UUID BLEChar = UUID.fromString("ca73b3ba-39f6-4ab3-91ae-186dc9577d99");
    private BluetoothGatt mGatt;
    private Button sunscreenButton;
   // private Button startButton;
    private Button inSunButton;
    private TextView timerTextView;
    private TextView timerTextView2;
    private ImageButton settings;
    private int seconds;
    private int seconds2 = 0;
    private boolean timerRunning = false;
    private boolean timerRunning2 = false;
    private EditText spfEditText;
    private int spf;
    private int count = 0;
    private float UVsum = 1;
    private float UVcount = 1;
    private float UVaverage = 2;
    private RelativeLayout mLoadingScreen;
    private TextView uvtextview2,uvtitletext;
    private com.scwang.wave.MultiWaveHeader wave;
    private TextView uvValueTextView, timertotal, totaltime;
    private TextView uvText, tempText, expUV;
    private GraphView graph;
    private int seconds3 = 0;
    private boolean timerRunning3 = false;
    //graph.setVisibility(View.VISIBLE);

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String latitude, longitude;

    // weather url to get JSON
    String weather_url1 = "";

    // api id for url
    String api_id1 = "83264ea1a2b54a48a36346a0ef705e42";

    private TextView textView;
    private FusedLocationProviderClient fusedLocationClient;

    private
    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(0, 0),

    });

    ActivityResultLauncher<Intent> googlePickerActivityResultPicker;

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        public class networkThread implements Runnable {
            AccountManagerFuture<Bundle> result;

            public networkThread(AccountManagerFuture<Bundle> result) {
                this.result = result;
            }

            @Override
            public void run() {
                textView  = findViewById(R.id.user);
                try {
                    Intent launch = (Intent) result.getResult().get(AccountManager.KEY_INTENT);
                    if (launch != null) {
                        googlePickerActivityResultPicker.launch(launch);
                        return;
                    }
                    Bundle bundle = null;
                    bundle = result.getResult();
// The token is a named value in the bundle. The name of the value
// is stored in the constant AccountManager.KEY_AUTHTOKEN.
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    textView.setText(token);
                    System.out.println("Token Received: " + token);
                }
                catch (OperationCanceledException e) {
                    throw new RuntimeException(e);
                } catch (AuthenticatorException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        @Override
        public void run(AccountManagerFuture accountManagerFuture){
            networkThread networkThread = new networkThread(accountManagerFuture);
            networkThread.run();
        }
    }



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AccountManager accountManager =
                AccountManager.get(getApplicationContext());
        Intent googlePicker = AccountManager.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, null, null,
                null, null);
        googlePickerActivityResultPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Bundle options = new Bundle();
                            AccountManager accountManager = AccountManager.get(MainActivity.this);
                            Account[] accounts = accountManager.getAccounts();
                            for (Account a : accounts) {
                                Log.d("TAG", "type--- " + a.type + " ---- name---- " + a.name);
                               accountManager.invalidateAuthToken(a.type, null);
                                accountManager.getAuthToken(a, "Manage your tasks",
                                        options, MainActivity.this, new OnTokenAcquired(),
                                        new Handler()); // Callback called if an error occurs
                           }
                        }
                    }
                }
        );




        mLoadingScreen = findViewById(R.id.loading_screen);
        mLoadingScreen.setVisibility(View.VISIBLE);
        settings = findViewById(R.id.settingsButton);
        timerTextView2 = findViewById(R.id.timerTextView2);
        sunscreenButton = findViewById(R.id.sunscreenButton);
        wave = findViewById(R.id.waveHeader);
        uvtextview2 = findViewById(R.id.uv_text_view2);
        uvtitletext = findViewById(R.id.uvTitleTextView);
        inSunButton = findViewById(R.id.inSunButton);
        uvValueTextView = findViewById(R.id.uvValueTextView);
        graph = (GraphView) findViewById(R.id.graph);
        timertotal = findViewById(R.id.timertotal);
        totaltime = findViewById(R.id.totaltime);
        tempText = findViewById(R.id.tempText);
        uvText = findViewById(R.id.uvText);
        expUV = findViewById(R.id.expUV);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e("lat", weather_url1);

        Log.e("lat", "onClick");
        // function to find the coordinates
        // of the last location
        obtainLocation();

        // Use a Handler and Runnable to simulate a delay in loading
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                googlePickerActivityResultPicker.launch(googlePicker);
                // Remove the loading screen when loading is complete
                mLoadingScreen.setVisibility(View.GONE);
                wave.setVisibility(View.VISIBLE);
                settings.setVisibility(View.VISIBLE);
                wave.setVisibility(View.VISIBLE);
                timerTextView2.setVisibility(View.VISIBLE);
                sunscreenButton.setVisibility(View.VISIBLE);
                uvtextview2.setVisibility(View.VISIBLE);
                uvtitletext.setVisibility(View.VISIBLE);
                inSunButton.setVisibility(View.VISIBLE);
                uvValueTextView.setVisibility(View.VISIBLE);
                graph.setVisibility(View.VISIBLE);
                graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
                graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                graph.getGridLabelRenderer().setGridColor(Color.BLACK);
                graph.getGridLabelRenderer().reloadStyles();
                timertotal.setVisibility(View.VISIBLE);
                totaltime.setVisibility(View.VISIBLE);
                uvText.setVisibility(View.VISIBLE);
                tempText.setVisibility(View.VISIBLE);
                expUV.setVisibility(View.VISIBLE);

            }
        }, 2500);


        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("F4:12:FA:4F:A5:71");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mGatt = device.connectGatt(this, false, gattCallback);

        //startButton = findViewById(R.id.startButton);
        //stopButton = findViewById(R.id.stopButton);
       // timerTextView = findViewById(R.id.timerTextView);


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, mainSettings.class);
                startActivity(intent);
            }
        });

        sunscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!timerRunning) {
//                    EditText spfEditText = findViewById(R.id.spfEditText);
//                    String spfString = spfEditText.getText().toString();
//
//                    if (!spfString.isEmpty()) {
//                        spf = Integer.parseInt(spfString);
//                    }
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    int spf = prefs.getInt("spf", 50);

                    Log.d(TAG, "SPF input: " + spf);
                    if(spf == 0){
                        Toast.makeText(getApplicationContext(), "Please set SPF in settings",
                                Toast.LENGTH_LONG).show();

                    }else if (UVaverage == 0){
                        seconds = (600*spf);
                    }
                    else{
                        seconds = (600*spf)/(int)UVaverage;
                    }
                    timerRunning = true;
                    startTimer2();
                } else {
//                    EditText spfEditText = findViewById(R.id.spfEditText);
//                    String spfString = spfEditText.getText().toString();
//
//                    if (!spfString.isEmpty()) {
//                        spf = Integer.parseInt(spfString);
//                    }
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    int spf = prefs.getInt("spf", 50);

                    Log.d(TAG, "SPF input: " + spf);
                    seconds = 300*spf;
                }
            }
        });

        inSunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning2) {
                    timerRunning2 = true;
                    startTimer();
                } else {
                    seconds2 = 0;
                }
            }
        });



//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                seconds = 0;
//                seconds2 = 0;
//                timerRunning = false;
//                timerRunning2=false;
//            }
//        });





    }

    @SuppressLint("MissingPermission")
    private void obtainLocation() {

        ActivityCompat.requestPermissions( this,
                new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
           // Log.e("loc", longitude);
            weather_url1 = "https://api.weatherbit.io/v2.0/current?" + "lat=" + latitude + "&lon=" + longitude + "&key=" + api_id1 + "&units=" + "I";
            Log.e("lat", weather_url1);
            // this function will
            // fetch data from URL
            getConditions();
        }

        /*Log.e("lat", "function");
        // get the last location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // get the latitude and longitude
                        // and create the http URL
                        weather_url1 = "https://api.weatherbit.io/v2.0/current?" + "lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&key=" + api_id1;
                        Log.e("lat", weather_url1);
                        // this function will
                        // fetch data from URL
                        getConditions();
                    }
                }); */
    }

    public void getConditions() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = weather_url1;
        Log.e("lat", url);

        // Request a string response
        // from the provided URL.
        StringRequest stringReq = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("lat", response);
                        try {
                            // get the JSON object
                            JSONObject obj = new JSONObject(response);

                            // get the Array from obj of name - "data"
                            JSONArray arr = obj.getJSONArray("data");
                            Log.e("lat obj1", arr.toString());

                            // get the JSON object from the
                            // array at index position 0
                            JSONObject obj2 = arr.getJSONObject(0);
                            Log.e("lat obj2", obj2.toString());

                            // set the temperature and the city
                            // name using getString() function
                            tempText.setText(obj2.getString("temp") + "° in " + obj2.getString("city_name"));
                            uvText.setText(obj2.getString("uv").substring(0,3));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                // In case of any error
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tempText.setText("That didn't work!");
                    }
                });
        queue.add(stringReq);
    }



    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null ) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        gatt.setCharacteristicNotification(characteristic, true);

                    }
                }
            } else {
                Log.d(TAG, "Service discovery failed.");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("characterisitc", String.valueOf(characteristic));
            byte[] values = characteristic.getValue();
            Log.d("Byte Array", String.valueOf((char)values[1]+(char)values[2] + "."+ (char)values[4] + (char)values[5]));
            String UVindex = (char)values[2] + "."+ (char)values[4] + (char)values[5];
            String UVindex1 = (char)values[1] + "."+(char)values[2] +  (char)values[4];
            float UV = Float.parseFloat(UVindex);
            float UV1 = Float.parseFloat(UVindex1);
            if(UV > 8){
                if (!timerRunning3) {
                    timerRunning3 = true;
                    startTimer3();
                }
            }else{
                timerRunning3=false;
                seconds3 = 0;
            }
            Log.d(TAG, "Characteristic changed: " + UV);
            Log.d(TAG, "Characteristic changed1: " + UV1);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if(UV >= 100){
//                        uvValueTextView.setText(String.valueOf(UV1));
//                    }
//                    else{uvValueTextView.setText(String.valueOf(UV));}
                    uvValueTextView.setText(String.valueOf(UV));
                }
            });

            UVsum = UVsum + UV;
            UVcount++;
            UVaverage = UVsum / UVcount;

            series.setColor(0xFFff973c);
            series.setBackgroundColor(0xFFff973c);
            series.appendData(new DataPoint(count, UV), true, 10000);
            count++;
            graph.addSeries(series);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mGatt.disconnect();
    }

    private void startTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                int hours2 = seconds2 / 3600;
                int minutes2 = (seconds2 % 3600) / 60;
                int remainingSeconds2 = seconds2 % 60;
                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours2,minutes2, remainingSeconds2);
                TextView timertotal = findViewById(R.id.timertotal);
                timertotal.setText(time);
                if (timerRunning2) {
                    seconds2++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void startTimer2() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int remainingSeconds = seconds % 60;
                String time2 = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours,minutes, remainingSeconds);
                timerTextView2.setText(time2);
                if(seconds <= 300){
                    String CHANNEL_ID = "1";
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.uvlogo3)
                            .setContentTitle("Timer has ended")
                            .setContentText("Your timer has finished!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setVibrate(new long[]{0, 1000, 500, 1000});

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    int notificationId = 1;
                    notificationManager.notify(notificationId, builder.build());

                    Toast.makeText(getApplicationContext(), "Reapply Sunscreen in 5 Min!",
                            Toast.LENGTH_LONG).show();
                }
                if (timerRunning && seconds > 0) {
                    seconds--;
                    handler.postDelayed(this, 1000);
                }
            }
        });


    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTimer3() {
        final Handler handler3 = new Handler();
        handler3.post(new Runnable() {

            @Override
            public void run() {
                int hours3 = seconds3 / 3600;
                int minutes3 = (seconds3 % 3600) / 60;
                int remainingSeconds3 = seconds3 % 60;
                if(minutes3>= 15){
                    Toast.makeText(getApplicationContext(), "DANGER: High UV For Extended Period of Time!",
                            Toast.LENGTH_LONG).show();
                }
                if (timerRunning3) {
                    seconds3++;
                    handler3.postDelayed(this, 1000);
                }
            }
        });
    }




}


