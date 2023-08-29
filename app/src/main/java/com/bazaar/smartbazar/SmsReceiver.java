package com.bazaar.smartbazar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            String verificationCode = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            if (verificationCode != null) {
                // Show a toast notification with the verification code
                Toast.makeText(context, "Received verification code: " + verificationCode, Toast.LENGTH_SHORT).show();
            }
        }
    }
}



