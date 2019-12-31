package com.example.a0910_map_pre;

import android.os.AsyncTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


class asyncTask_Bus extends AsyncTask<String, Void, ArrayList<String>> {
    private ArrayList<String> arrayList_Bus = new ArrayList<String>();
    private int count_station = 0;


    private String[] info_Station = { // 버스정류장 정보 표시용 선택자
            "#daumContent > div.list_content_wrap > div.bus_info > div.default_info_wrap > h3",
            "#daumContent > div.list_content_wrap > div.bus_info > div.default_info_wrap > span > span:nth-child(1)",
            "#daumContent > div.list_content_wrap > div.bus_info > div.comingsoon_wrap > h4",
            "#daumContent > div.list_content_wrap > div.bus_info > div.comingsoon_wrap > span"
    };

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        arrayList_Bus.clear();
    }

    @Override
    protected ArrayList<String> doInBackground(String... url_Bus) {
        try {
//                Log.e("살려줘 이게아닌가봐", url_Bus[0]);
            Document html_Bus = Jsoup.connect(url_Bus[0]).get(); //naver페이지를 불러옴
            Element temp;
            for (String string : info_Station) {  // 정류장이름, 방향(split 필요함), 곧도착
                temp = html_Bus.selectFirst(string);
                try {
                    arrayList_Bus.add(temp.text());
                } catch (Exception e) {
                    arrayList_Bus.add(" ");
                }

            }
            Elements numberOf_Bus = html_Bus.select("#daumContent > div.list_content_wrap > ul.list_result.bus_list_wrap > li");//셀렉터로 span태그중 class값이 ah_k인 내용을 가져옴
            for (int count_i = 1; count_i <= numberOf_Bus.size(); count_i++) {

                String[] info_Bus = { // 각각 버스의 이름과 도착예정 시간
                        "#daumContent > div.list_content_wrap > ul.list_result.bus_list_wrap > li:nth-child(" + count_i + ") > a > span.info_relbus > span.txt_tit > strong",
                        "#daumContent > div.list_content_wrap > ul.list_result.bus_list_wrap > li:nth-child(" + count_i + ") > a > span.info_situation > span:nth-child(1)",
                        "#daumContent > div.list_content_wrap > ul.list_result.bus_list_wrap > li:nth-child(" + count_i + ") > a > span.info_situation > span:nth-child(2)"
                };
                for (String string : info_Bus) {
                    temp = html_Bus.selectFirst(string);
                    try {
                        arrayList_Bus.add(temp.text());
                    } catch (Exception e) {
                        arrayList_Bus.add("정보없음");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList_Bus;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        arrayList_Bus.clear();
    }
}