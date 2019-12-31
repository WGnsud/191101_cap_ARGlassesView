package com.example.a0910_map_pre;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

class asyncTask_Map extends AsyncTask<String, Void, ArrayList<String>> {
    private ArrayList<String> way_navi = new ArrayList<>();
    private Elements contents;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
    }

    @Override
    protected ArrayList<String> doInBackground(String... url_Map) {
        try {
            Document doc = Jsoup.connect(url_Map[0]).get(); // 웹뷰에서 지정된 URL을 로드- 길안내 페이지
            contents = doc.select("a > span.txt_section"); //셀렉터로 span태그중 class값이 ah_k인 내용을 가져옴
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Element element: contents) {
            way_navi.add(element.text());
        }
        return way_navi;
    }
}