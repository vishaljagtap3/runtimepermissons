package in.bitcode.runtimepermissions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private Button mBtnSendSMS, mBtnReadExtStorage, mBtnCallPhone;
    private final int REQ_SMS_CAMERA = 1, REQ_CALL_PHONE = 2, REQ_EXT_STORAGE = 3;

    private BroadcastReceiver mBrSent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Snackbar.make(mBtnCallPhone, "SMS Sent", Snackbar.LENGTH_LONG).show();
        }
    };
    private BroadcastReceiver mBrDelivered = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Snackbar.make(mBtnCallPhone, "SMS Delivered", Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnSendSMS = findViewById(R.id.btnSendSMS);
        mBtnReadExtStorage = findViewById(R.id.btnReadExtStorage);
        mBtnCallPhone = findViewById(R.id.btnCallPhone);

        registerReceiver(
                mBrSent,
                new IntentFilter("in.bitcode.sms.SENT")
        );
        registerReceiver(
                mBrDelivered,
                new IntentFilter("in.bitcode.sms.DELIVERED")
        );


        mBtnCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //call phone
                    callPhone();
                } else {
                    //req permission to call phone
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQ_CALL_PHONE
                    );
                }
            }
        });


        mBtnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int isGranted = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);
                if (isGranted == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                } else {
                    //req permission
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE},
                            REQ_SMS_CAMERA
                    );
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQ_SMS_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //code to send sms
                sendSMS();
            } else {
                Toast.makeText(this, "SMS can not be sent!", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == REQ_CALL_PHONE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhone();
            }
        }


    }

    @SuppressLint("MissingPermission")
    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel://9881900903"));
        startActivity(intent);
    }

    private void sendSMS() {

        PendingIntent sentIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent("in.bitcode.sms.SENT"),
                0
        );
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent("in.bitcode.sms.DELIVERED"),
                0
        );

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("9881900903", null, "Hello from BitCode", sentIntent, deliveredIntent);
        Snackbar.make(mBtnCallPhone, "SMS Sent to vishal", Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBrSent);
        unregisterReceiver(mBrDelivered);
        super.onDestroy();
    }
}