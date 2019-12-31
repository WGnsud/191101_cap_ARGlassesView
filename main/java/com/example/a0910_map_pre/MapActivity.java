package com.example.a0910_map_pre;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapActivity extends AppCompatActivity {
    WebView webView;
//    TextView way_text;
//    TextView textView; //결과를 띄어줄 TextView
    ImageButton reload; //reload버튼
    static ArrayList<String> way_navi = new ArrayList<String>();

    int way_num = 0;
    String cr_url = "https://m.map.kakao.com/actions/walkRoute?startLoc=%EB%AC%B8%ED%99%94%EC%A0%84%EB%8B%B9%EC%97%AD+%EA%B4%91%EC%A3%BC1%ED%98%B8%EC%84%A0&sxEnc=LWORQN&syEnc=LRVQLQ&endLoc=%EC%A1%B0%EC%84%A0%EB%8C%80%ED%95%99%EA%B5%90&exEnc=LWSLNS&eyEnc=LRQNOU&ids=P21160978%2CP182573322&service=#!";

     String crawl_url;
//     TextView html_text;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


//        html_text = findViewById(R.id.html_show);
//        way_text = (TextView) findViewById(R.id.way_text);
//        textView = (TextView) findViewById(R.id.textBox);
        reload = (ImageButton) findViewById(R.id.reload);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        // 자바스크립트인터페이스 연결
        // 이걸 통해 자바스크립트 내에서 자바함수에 접근할 수 있음.
//        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        // 페이지가 모두 로드되었을 때, 작업 정의
        //지정한 URL을 웹 뷰로 접근하기

        webView.loadUrl(cr_url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                crawl_url = url;
//                html_text.setText(crawl_url);
            }
        });

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                way_navi.clear();
                asyncTask_Map asyncTask_map = new asyncTask_Map();
                try {
                    way_navi.addAll(asyncTask_map.execute(crawl_url).get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                textView.setText("Now Loading...");
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    public void next(View view){
//        way_num++;
//        if(way_num>way_navi.size()){
//            way_num = 1;
//        }
//        way_text.setText("("+way_num+"/"+way_navi.size()+")\n"
//                +way_num+". "+way_navi.get(way_num-1));
//    }

}
