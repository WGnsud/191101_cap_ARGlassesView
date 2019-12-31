package com.example.a0910_map_pre;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class asyncTask_Weather extends AsyncTask<String, Void, ArrayList<String>> {
    private ArrayList<String> arrayList_Weather = new ArrayList<String>();
    private String a = new String("3");


    private String[] Weather_title = {"Location", "Weather", "Temperature", "probability", "Humidity", "wind speed"};
    private String[] Weather_info = {
            "#wob_loc",  //기준위치
//            "#wob_dts",  //기준날자 , 시간
            "#wob_dc",   //현재날씨
            "#wob_tm",   //온도
            "#wob_pp",   //강수확율
            "#wob_hm",    //습도
            "#wob_ws"
    };


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        arrayList_Weather.clear();
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);

    }

    @Override
    protected ArrayList<String> doInBackground(String... url_Weather) {
        try {
            int count = 0;
            String we_text;
            Document html_Weather = Jsoup.connect(url_Weather[0]).get(); //naver페이지를 불러옴
            for (String string : Weather_info) {
                Element temp = html_Weather.selectFirst(string);
                we_text = temp.text();
                try {
                    switch (we_text) {
                        case "맑음":
                            we_text = "sunny";
                            break;
                        case "대체로 맑음":
                            we_text = "Mostly Sunny";
                            break;
                        case "구름 조금":
                            we_text = "Partly Cloudy";
                            break;
                        case "흐림":
                            we_text = "Cloudy";
                            break;
                    }
                    arrayList_Weather.add(Weather_title[count] + ": " + we_text);
                    Log.e("삽입", we_text);
                    count++;
                } catch (Exception e) {
                    Log.e("오류 시발아", "날씨 정보 삽입에 문제있음");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList_Weather;
    }

    private void after_add() {
        int count = 0;

        for (String string : arrayList_Weather) {
            a = a + Weather_title[count] + ": " + string + ";";
            count++;
        }
        Log.e("날씨 전체 메시지", a);
    }
}