package com.example.a0910_map_pre;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
//원래 있던 주석들  나도잘 모름;ㅣㅣ

public class MySmsReceiver extends BroadcastReceiver {
    static String state_send = "";
    String sender;
    String content;
    public MySmsReceiver() { }


    /** * 안드로이드에 문자메시지가 도착할 경우 실행된다. * @param context * @param intent */
    @Override public void onReceive(Context context, Intent intent) {
        // SMS 메시지를 파싱합니다.
        Bundle bundle = intent.getExtras();
        if (bundle != null) { // 수신된 내용이 있으면
            // 실제 메세지는 Object타입의 배열에 PDU 형식으로 저장됨

            // 문자 메시지는 pdus란 종류 값으로 들어있음
            Object [] pdus = (Object[])bundle.get("pdus");

            SmsMessage[] msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // PDU 포맷으로 되어 있는 메시지를 복원합니다.
                msgs[i] = SmsMessage
                        .createFromPdu((byte[]) pdus[i]);

                sender = msgs[i].getOriginatingAddress();
                content = msgs[i].getMessageBody().toString();

                Log.i("sender", sender);
                Log.i("content", content);
                Toast.makeText(context, "message in "+sender+" "+content, Toast.LENGTH_SHORT).show();


                if(sender.equals("01000000000")){
                    int startIdx = content.indexOf("[");
                    int endIdx = content.indexOf("]");
                    String authNumber = content.substring(startIdx+1, endIdx);
                    Log.i("authNumber", authNumber);
                }
            }
            state_send = "SMS: "+sender+ "-"+content;
        }
    }
}
