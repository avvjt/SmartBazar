package com.bazaar.smartbazar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the broadcast is received
        if (intent.getAction() != null && intent.getAction().equals("com.bazaar.smartbazar.UNINSTALL")) {
            // Show a toast message indicating that uninstall is triggered
            Toast.makeText(context, "Uninstall Triggered!", Toast.LENGTH_SHORT).show();

            // You can also perform other cleanup actions or send data to a server here
            // However, remember that time might be very limited before the app is uninstalled
        }
    }
}
