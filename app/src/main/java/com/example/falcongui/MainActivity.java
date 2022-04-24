package com.example.falcongui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;


public class MainActivity extends AppCompatActivity {

    // creating a variable for
    // our Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our
    // Database Reference for Firebase.
    DatabaseReference databaseReference;
    DatabaseReference newRobotRef;

    // variables for Text view.
    private TextView poseW;
    private TextView poseX;
    private TextView poseY;

    // variables for robot icon
    private ImageView robotIcon;
    private String pose_w = "0";
    private String pose_x = "0";
    private String pose_y = "0";
    private float robotX = 0;
    private float robotY = 0;
    private float robotW = 0;
    private float mapX = 0;
    private float mapY =0;

    // variables for geotag
    private ImageView geoTag;
    private String interest_loc_x = "0";
    private String interest_loc_y = "0";
    private float geoX = 0;
    private float geoY = 0;
    private float mapGeoX = 0;
    private float mapGeoY = 0;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 100;

    //MQTT
    public String MQTTHOST = "tcp://10.10.10.100:1883";
    MqttAndroidClient client;
    MqttConnectOptions options;
    private Button mqttConnect;
    private boolean mqttConnected = false;
    private TextView mqttStatus;

    //Joystick
    private int Speed = 0;
    private MutableLiveData<Integer> publishControl = new MutableLiveData<>();
    private Button falconUp;
    private Button falconDown;
    private Boolean climbUp = false;
    private Boolean climbDown = false;
    private TextView flipperStatus;
    private TextView moveStatus;

    //Video Stream
    private static final String url = "rtsp://10.10.10.107:8554/mjpeg/1";
    private LibVLC libVlc;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;

    private TextView dateTime;
    private Button screenshot;

    /**
     *
     * ToDo: Button to switch between robots, Fetch/Micro
     * Done: Menu Popup page
     * ToDO: Falcon Micro flipper controls
     * ToDo: MQTT connect/disconnect button
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);

        // below line is used to get the instance
        // of our Firebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get
        // reference for our database.
        databaseReference = firebaseDatabase.getReference().child("robot");

        moveStatus = findViewById(R.id.moveStatus);
        JoystickView joystickLeft = findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                Log.d("Check_strength", String.valueOf(strength));
                Log.d("Check_angle", String.valueOf(angle));
                if (angle >= 45 && angle <= 135 && strength > 50) {
                    publishControl.setValue(4);
                    Log.d("publish", "left");
                    moveStatus.setText("MOVEMENT LEFT");
                } else if ((angle >= 315 && strength > 50) || (angle < 45 && strength > 50)) {
                    publishControl.setValue(1);
                    Log.d("publish", "forward");
                    moveStatus.setText("MOVEMENT FORWARD");
                } else if (angle < 315 && angle >= 225 && strength > 50) {
                    publishControl.setValue(3);
                    Log.d("publish", "right");
                    moveStatus.setText("MOVEMENT RIGHT");
                } else if (angle < 225 && angle >135 && strength > 50) {
                    publishControl.setValue(2);
                    Log.d("publish", "backward");
                    moveStatus.setText("MOVEMENT BACKWARD");
                } else {
                    publishControl.setValue(0);
                    moveStatus.setText("MOVEMENT STOP");
                }
            }
        });

        mqttConnect = findViewById(R.id.mqttConnect);
        mqttStatus = findViewById(R.id.mqttStatus);
        mqttConnect.setOnClickListener(v -> {
            mqttConnect(MQTTHOST, "falcon/drive");
            if (mqttConnected) {
                mqttStatus.setText("ROBOT CONNECTED");
                mqttStatus.setTextColor(getResources().getColor(R.color.green));
            }
        });

        publishControl.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newIntValue) {
                // Update the UI, in this case, a TextView.
                try {
                    Log.d("mqtt", "publishing " + publishControl.getValue().toString());
                    client.publish("falcon/drive", publishControl.getValue().toString().getBytes(StandardCharsets.UTF_8),0,false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        falconUp = findViewById(R.id.falconUp);
        flipperStatus = findViewById(R.id.flipperStatus);
        falconUp.setOnClickListener(v -> {
            if (climbUp == Boolean.FALSE) {
                publishControl.setValue(6);
                falconUp.setBackgroundResource(R.drawable.falcon_up_pressed);
                falconDown.setBackgroundResource(R.drawable.falcon_down);
                climbUp = true;
                climbDown = false;
                flipperStatus.setText("FLIPPER UP");
            } else {
                publishControl.setValue(0);
                falconUp.setBackgroundResource(R.drawable.falcon_up);
                climbUp = false;
                flipperStatus.setText("FLIPPER NEUTRAL");
            }
            Log.d("mqtt", "publishing " + publishControl.getValue().toString());
            try {
                Log.d("mqtt", "publishing " + publishControl.getValue().toString());
                client.publish("falcon/drive", publishControl.getValue().toString().getBytes(StandardCharsets.UTF_8),0,false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        falconDown = findViewById(R.id.falconDown);
        falconDown.setOnClickListener(v -> {
            if (climbDown == Boolean.FALSE) {
                publishControl.setValue(5);
                falconDown.setBackgroundResource(R.drawable.falcon_down_pressed);
                falconUp.setBackgroundResource(R.drawable.falcon_up);
                climbDown = true;
                climbUp = false;
                flipperStatus.setText("FLIPPER DOWN");
            } else {
                publishControl.setValue(0);
                falconDown.setBackgroundResource(R.drawable.falcon_down);
                climbDown = false;
                flipperStatus.setText("FLIPPER NEUTRAL");
            }
            try {
                Log.d("mqtt", "publishing " + publishControl.getValue().toString());
                client.publish("falcon/drive", publishControl.getValue().toString().getBytes(StandardCharsets.UTF_8),0,false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });

        libVlc = new LibVLC(this);
        mediaPlayer = new MediaPlayer(libVlc);
        videoLayout = findViewById(R.id.videoLayout);
        mediaPlayer.attachViews(videoLayout, null, false, false);

        Media media = new Media(libVlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=600");

        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();

        dateTime = findViewById(R.id.dateTime);
        setDate(dateTime);

        screenshot = findViewById(R.id.screenshot);
        screenshot.setOnClickListener(v -> {
            takeScreenshot();
        });
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        mediaPlayer.stop();
        mediaPlayer.detachViews();
    }


    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                Log.i("check_time", "time_updated");
//                updateRobotIcon();
            }
        }, delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mediaPlayer.release();
        libVlc.release();
    }

    public void mqttConnect(String MQTTHost, String subscription) {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this, MQTTHost, clientId);
        options = new MqttConnectOptions();

        try {
            Log.i("mqtt", "attempting connection");
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.i("mqtt","connected");

                    setSubscription(subscription);

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.i("mqtt", "did not connect");
                }
            });
        } catch (MqttException e) {
            Log.i("mqtt", "exception");
            e.printStackTrace();
        }
    }

    private void setSubscription(String topicStr){
        try{
            client.subscribe(topicStr,0);
            Log.i("sub_test", "subscribing");
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    public void setDate (TextView view){
        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");//formating according to my need
        String date = formatter.format(today);
        view.setText(date);
    }

    private float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private void getdata() {

        // calling add value event listener method
        // for getting the values from database.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // this method is call to get the realtime
                // updates in the data.
                // this method is called when the data is
                // changed in our Firebase console.
                // below line is for getting the data from
                // snapshot of our database.
                pose_w = String.valueOf(snapshot.child("001").child("pose_w").getValue());
                pose_x = String.valueOf(snapshot.child("001").child("pose_x").getValue());
                pose_y = String.valueOf(snapshot.child("001").child("pose_y").getValue());
                interest_loc_x = String.valueOf(snapshot.child("001").child("Interest_Loc_x")
                        .child("0").getValue());
                interest_loc_y = String.valueOf(snapshot.child("001").child("Interest_Loc_y")
                        .child("0").getValue());

                // after getting the value we are setting
                // our value to our text view in below line.
                poseW.setText("X POSITION   " + pose_w);
                poseX.setText("Y POSITION   " + pose_x);
                poseY.setText("Z POSITION   " + pose_y);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}