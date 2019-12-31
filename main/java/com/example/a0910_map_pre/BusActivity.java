package com.example.a0910_map_pre;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.a0910_map_pre.MainActivity.arrayList_Bus;

public class BusActivity extends AppCompatActivity {
    int count_station = 0;
    EditText search_Bus;
//    TextView text_Bus;
    WebView view_Bus;
    String url_Bus;

    static String st_bus_url = "";


    // 지정한 버스정류장 이름, 방향, 곧도착,



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        search_Bus = (EditText) findViewById(R.id.input_search);
        search_Bus.setText("조선대학교");
//        text_Bus = (TextView) findViewById(R.id.text_bus);
        view_Bus = findViewById(R.id.web_Bus);
        view_Bus.getSettings().setJavaScriptEnabled(true);
        view_Bus.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                url_Bus = url;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
// 검색
    public void search(View view) {
        view_Bus.loadUrl("https://m.map.kakao.com/actions/searchView?q=" + search_Bus.getText().toString() + "&wxEnc=LQLMOP&wyEnc=LPVMSM&lvl=4&rcode=E1300A333#!/all/list/bus");
    }
// 표시
    public void show_way(View view) throws ExecutionException, InterruptedException {
        Log.e("URL: ", url_Bus);
        count_station = 0;
        st_bus_url = url_Bus;
        arrayList_Bus.clear();
        asyncTask_Bus asyncTask_bus = new asyncTask_Bus();
        arrayList_Bus.addAll(asyncTask_bus.execute(url_Bus).get());
//        text_Bus.setText(arrayList_Bus.get(0));

    }

}



