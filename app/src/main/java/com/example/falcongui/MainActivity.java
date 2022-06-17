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

//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.Media;
//import org.videolan.libvlc.MediaPlayer;
//import org.videolan.libvlc.util.VLCVideoLayout;


public class MainActivity extends AppCompatActivity {

    // creating a variable for
    // our Firebase Database.
//    FirebaseDatabase firebaseDatabase;

    // creating a variable for our
    // Database Reference for Firebase.
//    DatabaseReference databaseReference;
//    DatabaseReference newRobotRef;

    // variables for Text view.
    private TextView poseW;
    private TextView poseX;
    private TextView poseY;

    // variables for robot icon
//    private ImageView robotIcon;
//    private String pose_w = "0";
//    private String pose_x = "0";
//    private String pose_y = "0";
//    private float robotX = 0;
//    private float robotY = 0;
//    private float robotW = 0;
//    private float mapX = 0;
//    private float mapY =0;

    // variables for geotag
//    private ImageView geoTag;
//    private String interest_loc_x = "0";
//    private String interest_loc_y = "0";
//    private float geoX = 0;
//    private float geoY = 0;
//    private float mapGeoX = 0;
//    private float mapGeoY = 0;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000;
    boolean imuBool = false;
    boolean tofBool = false;

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
//    private static final String url = "rtsp://10.10.10.112:8554/mjpeg/1";
//    private LibVLC libVlc;
//    private MediaPlayer mediaPlayer;
//    private VLCVideoLayout videoLayout;
//
//    private static final String url2 = "rtsp://10.10.10.110:8554/mjpeg/1";
//    private LibVLC libVlc2;
//    private MediaPlayer mediaPlayer2;
//    private VLCVideoLayout videoLayout2;

    private TextView dateTime;
    private Button buzzer;
    private boolean buzz_press;
    private String buzz;

    private MutableLiveData<Float> pitchY = new MutableLiveData<>();
    private ImageView orientation;
    private TextView tofValue;
    private TextView bms;

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
//        firebaseDatabase = FirebaseDatabase.getInstance();
//
//        // below line is used to get
//        // reference for our database.
//        databaseReference = firebaseDatabase.getReference().child("robot");

        // initializing our object class variable.
//        poseW = findViewById(R.id.pose_w);
//        poseX = findViewById(R.id.pose_x);
//        poseY = findViewById(R.id.pose_y);
//        robotIcon = findViewById(R.id.robotIcon);
//        geoTag = findViewById(R.id.geoTag);

        // calling method
        // for getting data.
//        getdata();
//        updateRobotIcon();
        //updateGeotags();
        //addNewRobot("002");
        //removeRobot("002");

//        Button popupButton = findViewById(R.id.buttonPopup);
//        popupButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                PopUpWindow popUpClass = new PopUpWindow();
//                popUpClass.showPopupWindow(v);
//            }
//        });

        moveStatus = findViewById(R.id.moveStatus);
        JoystickView joystickLeft = findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                Log.d("Check_strength", String.valueOf(strength));
                Log.d("Check_angle", String.valueOf(angle));
                if (angle >= 45 && angle <= 135 && strength > 50) {
                    publishControl.setValue(1);
                    Log.d("publish", "forward");
                    moveStatus.setText("MOVEMENT FORWARD");
                } else if ((angle >= 315 && strength > 50) || (angle < 45 && strength > 50)) {
                    publishControl.setValue(4);
                    Log.d("publish", "right");
                    moveStatus.setText("MOVEMENT RIGHT");
                } else if (angle < 315 && angle >= 225 && strength > 50) {
                    publishControl.setValue(2);
                    Log.d("publish", "backward");
                    moveStatus.setText("MOVEMENT BACKWARD");
                } else if (angle < 225 && angle >135 && strength > 50) {
                    publishControl.setValue(3);
                    Log.d("publish", "left");
                    moveStatus.setText("MOVEMENT LEFT");
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
                mqttConnect.setVisibility(View.GONE);
            }
        });

        publishControl.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newIntValue) {
                // Update the UI, in this case, a TextView.

                try {
                    Log.d("mqtt", "publishing " + publishControl.getValue().toString());
                    client.publish("falcon/drive", publishControl.getValue().toString().getBytes(StandardCharsets.UTF_8), 0, false);
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

        orientation = findViewById(R.id.orientation);
        tofValue = findViewById(R.id.tofValue);
        pitchY.observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable final Float newFloatValue) {
                // Update the UI, in this case, an Image.
                Log.i("ORIENTATION", String.valueOf(pitchY.getValue()));
                orientation.setRotation(pitchY.getValue());
            }
        });

        bms = findViewById(R.id.bmsValue);

//        ArrayList<String> vlcOptions = new ArrayList<>();
//        vlcOptions.add("--file-caching=2000");
//        //vlcOptions.add("--rtp-caching=0");
//        vlcOptions.add("-vvv");
        //vlcOptions.add(":network-caching=300");
        //vlcOptions.add(":sout = #transcode{vcodec=x264,vb=800,scale=0.25,acodec=none,fps=23}:display :no-sout-rtp-sap :no-sout-standard-sap :ttl=1 :sout-keep");

        //First Video feed
//        libVlc = new LibVLC(this, vlcOptions);
//        mediaPlayer = new MediaPlayer(libVlc);
//        videoLayout = findViewById(R.id.videoLayout);
//        mediaPlayer.attachViews(videoLayout, null, false, false);
//
//        Media media = new Media(libVlc, Uri.parse(url));
//        media.setHWDecoderEnabled(true, false);
//        media.addOption(":network-caching=150");
//        media.addOption(":clock-jitter=0");
//        media.addOption(":clock-synchro=0");
//        //media.addOption(":file-caching=0");
//        //media.addOption(":network-caching=300");
//        //media.addOption(":sout = #transcode{vcodec=x264,vb=800,scale=0.25,acodec=none,fps=23}:display :no-sout-rtp-sap :no-sout-standard-sap :ttl=1 :sout-keep");
//
//
//        mediaPlayer.setMedia(media);
//        media.release();
//        mediaPlayer.play();
//
//        //Second Video feed
//        libVlc2 = new LibVLC(this, vlcOptions);
//        mediaPlayer2 = new MediaPlayer(libVlc2);
//        videoLayout2 = findViewById(R.id.videoLayout2);
//        mediaPlayer2.attachViews(videoLayout2, null, false, false);
//
//        Media media2 = new Media(libVlc2, Uri.parse(url2));
//        media2.setHWDecoderEnabled(true, false);
//        media2.addOption(":network-caching=600");
//        media2.addOption(":clock-jitter=0");
//        media2.addOption(":clock-synchro=0");
        //media2.addOption(":file-caching=0");
        //media2.addOption(":network-caching=300");
        //media2.addOption(":sout = #transcode{vcodec=x264,vb=800,scale=0.25,acodec=none,fps=23}:display :no-sout-rtp-sap :no-sout-standard-sap :ttl=1 :sout-keep");


//        mediaPlayer2.setMedia(media2);
//        media2.release();
//        mediaPlayer2.play();

        dateTime = findViewById(R.id.dateTime);
        setDate(dateTime);

        buzzer = findViewById(R.id.buzzer);
        buzzer.setOnClickListener(v -> {
            if (buzz_press) {
                buzz_press = false;
                buzz = "0";
            } else {
                buzz_press = true;
                buzz = "1";
            }
            try {
                //QOS
                client.publish("falcon/buzzer", buzz.getBytes(StandardCharsets.UTF_8),0,false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }


//    @Override
//    protected void onStop()
//    {
//        super.onStop();
//
//        mediaPlayer.stop();
//        mediaPlayer.detachViews();
//    }


    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                //Log.i("check_time", "time_updated");
//                updateRobotIcon();
                if (!imuBool) {
                    imuBool = true;
                    tofBool = true;
                }
            }
        }, delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
//        mediaPlayer.release();
//        libVlc.release();
    }
    //MQTT
    public void mqttConnect(String MQTTHost, String subscription) {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this, MQTTHost, clientId);
        options = new MqttConnectOptions();
        options.setUserName("username");
        String mqttPassword = "password";
        options.setPassword(mqttPassword.toCharArray());

        try {
            Log.i("mqtt", "attempting connection");
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.i("mqtt","connected");
                    //setSubscription(subscription);
                    setSubscription("esp32/y_value");
                    setSubscription("esp32/tof_value");
                    setSubscription("esp32/BMS");
                    mqttConnected = true;


                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.i("mqtt", "did not connect");
                }
            });
            //if (client.isConnected()) {
                // MQTT RECEIVE
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) { }

                    //MQTT RECEIVE MESSAGE
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.i("RECEIVED", new String(message.getPayload()));
                        Log.i("topic", topic);

                        if (topic.equals("esp32/y_value")){

                            String pitchYString = new String(message.getPayload());
                            //Log.i("imu", pitchYString);;
                            pitchY.setValue(Float.parseFloat(pitchYString) * 57.0f);
                            Log.i("imu", pitchYString);
                            //orientation.setRotation(Float.parseFloat(pitchYString) * 57.0f);
                            imuBool = false;
                            //Log.i("imu", Integer.getInteger(pitchYString) * 57);
                            //Log.i("IMU", String.valueOf(Float.parseFloat(pitchYString) * 57));
        //                    JsonObject weight = new Gson().fromJson(sensor_weight_value, JsonObject.class);
        //                    float weight_result = weight.get("weight").getAsFloat();
        //                    Log.i("weight_value", String.valueOf(weight_result));
        //                    DetailActivity.weight_value = weight_result;
                        }

                        if (topic.equals("esp32/tof_value")) {
                            String tof_value = new String(message.getPayload());
                            Log.i("tof", tof_value);
                            tofValue.setText("TOF: " + tof_value);
        //                    JsonObject weight = new Gson().fromJson(sensor_oxygen_value, JsonObject.class);
        //                    float oxygen_result = weight.get("slpm").getAsFloat();
        //                    Log.i("oxygen_test", String.valueOf(oxygen_result));
        //                    oxygen_shortTimeStr = oxygen_sdf.format(oxygen_currTime);
        //                    flow_rate = oxygen_result;
                            tofBool = false;
                        }
                        //TODO: BMS
                        if (topic.equals("esp32/BMS")) {
                            String bms_value = new String(message.getPayload());
                            //Log.i("tof", bms_value);
                            bms.setText("Battery: " + bms_value);
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) { }
                });
           // }
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

    // Add a new robot to firebase
//    private void addNewRobot(String robotID) {
//        newRobotRef = firebaseDatabase.getReference().child("robot").child(robotID);
//        Robot robot = new Robot(robotID);
//        newRobotRef.setValue(robot);
//    }
//
//    // Remove a specific robot from firebase
//    private void removeRobot(String robotID) {
//        newRobotRef = firebaseDatabase.getReference().child("robot").child(robotID);
//        newRobotRef.removeValue();
//    }
//
//    // Update the robot's position on the minimap
//    private void updateRobotIcon() {
//        robotX = Float.parseFloat(pose_x);
//        robotY = Float.parseFloat(pose_y);
//        mapX = map(robotX, -5, 5, 240, 1400);
//        mapY = map(robotY, -5, 5, 220, 800);
//        //850 x 500 center
//        robotIcon.setTranslationX(mapX);
//        robotIcon.setTranslationY(mapY);
//    }
//
//    // Update the positions of geotags
//    private void updateGeotags() {
//        geoX = Float.parseFloat(interest_loc_x);
//        geoY = Float.parseFloat(interest_loc_y);
//        mapGeoX = map(geoX, -50, 50, 0, 1700);
//        mapGeoY = map(geoY, -50, 50, 0, 1000);
//        geoTag.setTranslationX(mapGeoX);
//        geoTag.setTranslationY(mapGeoY);
//    }
//
//    private void takeScreenshot() {
//        Date now = new Date();
//        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//
//        try {
//            // image naming and path  to include sd card  appending name you choose for file
//            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
//
//            // create bitmap screen capture
//            View v1 = getWindow().getDecorView().getRootView();
//            v1.setDrawingCacheEnabled(true);
//            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
//            v1.setDrawingCacheEnabled(false);
//
//            File imageFile = new File(mPath);
//
//            FileOutputStream outputStream = new FileOutputStream(imageFile);
//            int quality = 100;
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//            outputStream.flush();
//            outputStream.close();
//
//        } catch (Throwable e) {
//            // Several error may come out with file handling or DOM
//            e.printStackTrace();
//        }
//    }

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

//    private void getdata() {
//
//        // calling add value event listener method
//        // for getting the values from database.
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // this method is call to get the realtime
//                // updates in the data.
//                // this method is called when the data is
//                // changed in our Firebase console.
//                // below line is for getting the data from
//                // snapshot of our database.
//                pose_w = String.valueOf(snapshot.child("001").child("pose_w").getValue());
//                pose_x = String.valueOf(snapshot.child("001").child("pose_x").getValue());
//                pose_y = String.valueOf(snapshot.child("001").child("pose_y").getValue());
//                interest_loc_x = String.valueOf(snapshot.child("001").child("Interest_Loc_x")
//                        .child("0").getValue());
//                interest_loc_y = String.valueOf(snapshot.child("001").child("Interest_Loc_y")
//                        .child("0").getValue());
//
//                // after getting the value we are setting
//                // our value to our text view in below line.
//                poseW.setText("X POSITION   " + pose_w);
//                poseX.setText("Y POSITION   " + pose_x);
//                poseY.setText("Z POSITION   " + pose_y);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // calling on cancelled method when we receive
//                // any error or we are not able to get the data.
//                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}