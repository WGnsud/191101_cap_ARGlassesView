package com.example.a0910_map_pre;

import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import static com.example.a0910_map_pre.MySmsReceiver.state_send;

public class PhoneStateCheckListener extends PhoneStateListener {
    MainActivity mainActivity;

    PhoneStateCheckListener(MainActivity _main) {
        mainActivity = _main;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            Toast.makeText(mainActivity, "STATE_IDLE : " + incomingNumber, Toast.LENGTH_SHORT).show();
            Log.e("전화 ", "IDLE" + incomingNumber);

        } else if (state == TelephonyManager.CALL_STATE_RINGING) {
            Toast.makeText(mainActivity, "STATE_RINGING : " + incomingNumber, Toast.LENGTH_SHORT).show();
            Log.e("전화", "RINGING" + incomingNumber);
            state_send = "CALL : " + incomingNumber;
//수신 부분 입니다.

        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            Toast.makeText(mainActivity, "STATE_OFFHOOK :" + incomingNumber, Toast.LENGTH_SHORT).show();
            Log.e("전화", "OFFHOOK" + incomingNumber);
        }
    }

}
