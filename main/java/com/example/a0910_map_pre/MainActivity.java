package com.example.a0910_map_pre;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import static com.example.a0910_map_pre.BusActivity.st_bus_url;
import static com.example.a0910_map_pre.MapActivity.way_navi;
import static com.example.a0910_map_pre.MySmsReceiver.state_send;
import static com.example.a0910_map_pre.StepActivity.step_count;

public class MainActivity extends AppCompatActivity {
    static boolean Thread_run;
    Thread time_Thread;     //일정 주기로 반복될 쓰레드
    Switch time_Choos; //시간표시 선택 스위치
    ImageButton  recognizer_button;
    String c_url = "https://www.google.com/search?q=날씨";

    TextView led_view1,led_view2, led_view3;
    TextView time_Check;
    static TextView show_step;
    String last_menu = "0";
    int bus_count = -3;
    int map_count = -1;
    int weather_count = 0;
    PhoneStateCheckListener phoneCheckListener;
    static ArrayList<String> arrayList_Bus = new ArrayList<String>();
    ArrayList<String> arrayList_weather = new ArrayList<String>();

    public BluetoothSPP bt; //BluetoothSPP 선언 bt

    Intent i;
    SpeechRecognizer mRecognizer;
//    TextView Recognizer_textView;

    public void onStart() { //엑티비티 시작시에
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        way_navi.clear();
        bt.stopService(); //블루투스 중지
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizer_button = findViewById(R.id.recognizer_button);

        led_view1 = findViewById(R.id.led_view1);
        led_view2 = findViewById(R.id.led_view2);
        led_view3 = findViewById(R.id.led_view3);

        step_count = "";
        show_step = findViewById(R.id.step_text);
        time_Check = findViewById(R.id.bt_time_txt);

//        Recognizer_textView = findViewById(R.id.Recognizer_text);
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");


        Bluetooth_start();
        permission_call(); //권한 체크
        phoneCheckListener = new PhoneStateCheckListener(this);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneCheckListener, PhoneStateListener.LISTEN_CALL_STATE);

        try {
            Refresh_all();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        time_Choos = (Switch) findViewById(R.id.time_Choose);
        time_Choos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {   //시간 선택 스위치 변경리스너
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Thread_run = isChecked; //pause
                Toast.makeText(MainActivity.this, getCurrentTime() + " " + isChecked, Toast.LENGTH_LONG).show(); //확인용 토스트메시지로 현재시간 + false/true 표시

            }
        });

        time_Thread = new Thread(new Runnable() {  //반복 쓰레드, 쓰레드 내부 동작 선언
            @Override
            public void run() {
                while (!time_Thread.interrupted()) //time_Thread 가 인터럽트 신호를 받기 전까지 동작함
                    try {
                        runOnUiThread(new Runnable() // start actions in UI thread // UI를 사용하기 위한 쓰레드
                        {
                            @Override
                            public void run() {
                                if (Thread_run) {
                                    if (last_menu.equals("0")) {
                                        bt.send(getCurrentTime(), true);
                                        bt.send(step_count + "step", true);
                                        bt.send(state_send, true);

                                        led_view1.setText(getCurrentTime());
                                        led_view2.setText(step_count + "step");
                                        led_view3.setText(state_send);
                                    }
                                    time_Check.setText(getCurrentTime());
                                }
                            }
                        });

                        time_Thread.sleep(2000); //반복 간격 2초
                    } catch (InterruptedException e) { //오류시 쓰레드 인터럽트
                        // ooops
                        time_Thread.interrupt();
                    }
            }
        }); // time_Thread 정의 끝
        time_Thread.start(); // 시작
    }


    //    ------------------------------------------------------------------------
    // 버튼정의 함수들
    public void Refrash(View view) throws ExecutionException, InterruptedException {
        Refresh_all();
    }

    public void goto_Map(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void goto_Bus(View view) {
        Intent intent = new Intent(this, BusActivity.class);
        startActivity(intent);
    }

    public void goto_Step(View view) {
        Intent intent = new Intent(this, StepActivity.class);
        startActivity(intent);
    }

    public void btnConnect(View view) {
        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void kill(View view) { //쓰레드 인터럽트(종료)
        Toast.makeText(MainActivity.this, "종료함", Toast.LENGTH_LONG).show();
        time_Thread.interrupt();

    }

    public void weather_prev(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"3", "prev"};
        Display(a);
    }

    public void weather_next(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"3", "next"};
        Display(a);
    }

    public void bus_prev(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"1", "prev"};
        Display(a);
    }

    public void bus_next(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"1", "next"};
        Display(a);
    }

    public void map_prev(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"2", "prev"};
        Display(a);
    }

    public void map_next(View view) throws InterruptedException { //쓰레드 인터럽트(종료)
        String a[] = {"2", "next"};
        Display(a);
    }

    public void Recognizer_Run(){
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(i);
    }

    public void Recognizer(View view) {
        // 음성인식 동작 테스트용 버튼
        Recognizer_Run();

    }

    //////음성인식 리스너
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onResults(Bundle results) {
            // TODO Auto-generated method stub
            // TODO 결과값 받기
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Log.e("음성인식", "" + rs[0]);
//            Recognizer_textView.setText("" + rs[0]);
            Recognizer_Classifier(rs[0]);
            recognizer_button.setBackgroundColor(Color.rgb(215,215,215));  //회색
        }


        @Override
        public void onReadyForSpeech(Bundle params) {
            //             TODO Auto-generated method stub
            Log.e("음성인식", "onReadyForSpeech");

//            Recognizer_textView.setText("onReadyForSpeech"); //음성인식 버튼을 눌럿을때
            recognizer_button.setBackgroundColor(Color.rgb(247,48,48)); //빨강
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
//             TODO Auto-generated method stub
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onError(int error) {
            Log.e("음성인식", "ERROR");

//            Recognizer_textView.setText("ERROR"); // 음성이 들어오지 않을때
            recognizer_button.setBackgroundColor(Color.rgb(215,215,215));  //회색
            // TODO Auto-generated method stub
        }

        @Override
        public void onEndOfSpeech() {
            // 입력이 끝나고 결과 출력전
            Log.e("음성인식", "onEndOfSpeech");
//            Recognizer_textView.setText("onEndOfSpeech");
            recognizer_button.setBackgroundColor(Color.rgb(16,255,49)); //녹색

//             TODO Auto-generated method stub
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.e("음성인식", "onBeginningOfSpeech");
//            Recognizer_textView.setText("onBeginningOfSpeech");
            recognizer_button.setBackgroundColor(Color.rgb(42,61,213)); //파랑

            // TODO Auto-generated method stub
        }
    };

    public void Recognizer_Classifier(String string) {
        String command_voice;
        switch (string) {
            case "다음": {
                command_voice = "vvnext";
                break;
            }
            case "메뉴": {
                command_voice = "vvmenu";
                break;
            }
            case "이전": {
                command_voice = "vvprev";
                break;
            }
            case "새로고침": {
                command_voice = "vvRefrash";
                break;
            }
            default: {
                command_voice = "vvERRER";
            }
        }
        bt.send("command", true);
        bt.send(command_voice, true);
        bt.send("commandEND", true);
    }

    //  블루투스--------------------------------------------------------------------------
    public void Bluetooth_start() {
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가 메시지
            Toast.makeText(MainActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신시
            public void onDataReceived(byte[] data, String message) {
//                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                try {
                    Display(message.split(",")); //스플릿 후 Display 함수 동작
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (message.equals("Refresh")) {
                    try {
                        Refresh_all();  // 전체 새로고침
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if(message.equals("voice_rec")){
                    // 음성인식 시작 버튼
                    Recognizer_Run();
                }
                Log.e("블루투스 수신 : ", message);
            }
        });

        //블루투스 연결 상태 관련
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext(), "Connected to " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                bt.send("system", true);
                bt.send("안드로이드 연결", true);
                bt.send("system", true);


                led_view1.setText("system");
                led_view2.setText("안드로이드 연결");
                led_view3.setText("system");
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        }); //블루투스 연결 관련 끝


    }

    //    ----------------------------------------------------------------------------------
//    퍼미션
    public void permission_call() {
        PermissionRequester.Builder requester = new PermissionRequester.Builder(this);  //SMS 수신 권한 클래스
        requester.create().request(Manifest.permission.RECEIVE_SMS, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {
                        Toast.makeText(MainActivity.this, "권한을 얻지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requester.create().request(Manifest.permission.ACCESS_FINE_LOCATION, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {
                        Toast.makeText(MainActivity.this, "권한을 얻지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requester.create().request(Manifest.permission.READ_PHONE_STATE, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {
                        Toast.makeText(MainActivity.this, "권한을 얻지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requester.create().request(Manifest.permission.RECORD_AUDIO, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                    @Override
                    public void onClick(Activity activity) {
                        Toast.makeText(MainActivity.this, "권한을 얻지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //-----------------------------------------------------------------------------------------------------
//    블루투스 관련
    public void onActivityResult(int requestCode, int resultCode, Intent data) {   // ;;;;;;잘;;
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);

        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //    ---------------------------------------------------------
//    시간 반환
    public String getCurrentTime() { // 현재시간 반환 함수
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("hh:mm:ss");
        String str = dayTime.format(new Date(time));
        return str;
    }

    public void show_time(View view) {
        time_Check.setText(getCurrentTime());

    }

    ///////////////////////////새로고침///////////////////////////////
    public void Refresh_all() throws ExecutionException, InterruptedException {
        Log.e("새로고침", "");
        call_bus(); // arrayList받고 저장
        call_Weather();
    }

    public void call_bus() throws ExecutionException, InterruptedException {
        if (!st_bus_url.equals("")) {
            arrayList_Bus.clear();
            asyncTask_Bus asyncTask_bus = new asyncTask_Bus();
            arrayList_Bus.addAll(asyncTask_bus.execute(st_bus_url).get());
        }
    }

    public void call_Weather() throws ExecutionException, InterruptedException {
        arrayList_weather.clear();
        Log.e("날씨 주소", c_url);
        asyncTask_Weather asyncTask_weather = new asyncTask_Weather();
        arrayList_weather.addAll(asyncTask_weather.execute(c_url).get());
    }

    //////////////////////////////화면 표시///////////////////////////////////////
    public void Display(String[] string) throws InterruptedException {
        last_menu = string[0];
        switch (string[0]) {  // 1: 버스, 2 : 길안내, 3 : 날씨
            case "0":
                Base_display(string[1]);
                break;
            case "1":
                Bus_display(string[1]);
                break;
            case "2":
                Map_display(string[1]);
                break;
            case "3":
                Weather_display(string[1]);
                break;
        }
    }

    public void Base_display(String string) throws InterruptedException {
        Log.e("첫화면 : ", getCurrentTime());
        Log.e("첫화면 : ", state_send);

        bt.send(getCurrentTime(), true);
        bt.send(step_count + "step", true);
        bt.send(state_send, true);

        led_view1.setText(getCurrentTime());
        led_view2.setText(step_count + "step");
        led_view3.setText(state_send);
    }

    public void Bus_display(String string) throws InterruptedException {  //3 줄 전송
        if (arrayList_Bus.size() != 0) {
            int calc = 0;
            String a, b, c, s[];
            Log.e("size : ", Integer.toString(arrayList_Bus.size()));
            switch (string) {
                case "next":
                    calc = 3;
                    break;
                case "prev":
                    calc = -3;
                    break;
                case "start":
                    calc = 0;
                    bus_count = 0;
                    break;
            }
            bus_count = bus_count + calc;

            if (bus_count == 1)
                bus_count = 0;
            else if (bus_count == 3)
                bus_count = 4;
            else if (bus_count < 0)
                bus_count = 0;


            if (bus_count == 0) {
                s = arrayList_Bus.get(1).split(" ");
                a = arrayList_Bus.get(0);
                b = rec_ko_ro(s[s.length - 1]);
                if (arrayList_Bus.get(2).equals(" ")) {
                    c = "No Arrivals";//arrayList_Bus.get(3);
                } else {
                    c = "Arrive:" + rec_ko_ro(arrayList_Bus.get(3));
                }
            } else {
                a = arrayList_Bus.get(bus_count);
                b = arrayList_Bus.get(bus_count + 1);
                String temp[] = b.split(" ");
                if (b.equals("곧도착"))
                    b = "Arrive";
                else if (temp.length>=4) {
                    b = temp[0] + temp[2] + temp[3];
                }

                c = arrayList_Bus.get(bus_count + 2);
                temp = c.split(" ");
                if (c.equals("곧도착"))
                    c = "Arrive";
                else if (temp.length>=4) {
                    c = temp[0] + temp[2] + temp[3];
                }
            }
            Log.e("버스 도착 정보: ", a);
            Log.e("버스 도착 정보: ", b);
            Log.e("버스 도착 정보: ", c);
            Log.e("--------------------", "----------------------------");
            ///로그챗을 블루투스로 보내는중
            bt.send(rec_ko_ro(a), true);
            bt.send(b, true);
            bt.send(c, true);


            led_view1.setText(a);
            led_view2.setText(b);
            led_view3.setText(c);
            if (bus_count + 2 == arrayList_Bus.size() - 1) {
                bus_count = -9;
            }
        } else
            Log.e("버스 디버그 : ", "버스 정보가 로드되지 않앗습니다.");
    }

    public void Map_display(String string) throws InterruptedException {  // 3 줄
        Log.e("지도 디버깅", Integer.toString(way_navi.size()));
        if (way_navi.size() != 0) {

            switch (string) {
                case "prev":
                    map_count = (map_count - 1) % way_navi.size();
                    if (map_count < 0)
                        map_count = 0;
                    break;
                case "next":
                    map_count = (map_count + 1) % way_navi.size();
                    break;
                case "start":
                    map_count = 0;
                    break;
            }
            Log.e("지도 디버깅 : ", ("(" + (map_count + 1) + "/" + Integer.toString(way_navi.size()) + ")"));
            Log.e("지도 디버깅 : ", way_navi.get(map_count));

            bt.send(Integer.toString(map_count + 1), true);
            bt.send(Integer.toString(way_navi.size()), true);
            bt.send(rec_ko_ro(way_navi.get(map_count)), true);


            led_view1.setText("("+map_count + 1+"/"+way_navi.size()+")");
            led_view3.setText(way_navi.get(map_count));
        } else
            Log.e("지도 디버깅 : ", "지도가 로드되지 않앗습니다.");

    }

    public void Weather_display(String string) throws InterruptedException { // 3줄 전송
        if (string.equals("start"))
            weather_count = 0;
        Log.e("날씨 디버그 : ", arrayList_weather.get(weather_count));
        Log.e("날씨 디버그 : ", arrayList_weather.get(weather_count + 1));
        Log.e("날씨 디버그 : ", arrayList_weather.get(weather_count + 2));

        bt.send(arrayList_weather.get(weather_count + 2), true);
        bt.send(arrayList_weather.get(weather_count + 1), true);
        bt.send(rec_ko_ro(arrayList_weather.get(weather_count)), true);


        led_view1.setText(arrayList_weather.get(weather_count + 2));
        led_view2.setText(arrayList_weather.get(weather_count + 1));
        led_view3.setText(rec_ko_ro(arrayList_weather.get(weather_count)));

        weather_count = (weather_count + 3) % 6;
    }

    public String rec_ko_ro(String string) {
        return KoreanRomanizer.romanize(string, KoreanCharacter.Type.District, KoreanCharacter.Type.Substantives, KoreanCharacter.ConsonantAssimilation.Regressive);
    }
}


