package ru.litres;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;


class Scrap {
    private static final String website = "https://www.litres.ru";
    private static JSONArray arr = new JSONArray();
    private static int volume = 10000;

    public static void scanItems(){
        Document document;
        int cnt = 0;
        int ind = 1;
        while(true) {
            try {
                document = Jsoup.connect(website + "/audioknigi/page-" + ind + "/").get();
            } catch (IOException ignored) {
                System.out.print("Couldn't scan items");
                return;
            }

            Elements elements = document.getElementsByClass("cover_href cover_200");
            for (Element element : elements) {
                cnt++;
                String link = element.attributes().get("href");
                arr.put(findProperties(link));
                if(cnt >= volume)
                    break;
            }
            if(cnt >= volume)
                break;
            System.out.println(ind);
            ind++;
        }
    }

    public static JSONObject findProperties(String link){
        JSONObject json = new JSONObject();
        Document document;
        try{
            document = Jsoup.connect(website + link).get();

        }
        catch (IOException ignored){
            System.out.print("Couldn't find properties");
            return null;
        }
        Elements elements = document.getElementsByClass("biblio_book_name biblio-book__title-block");
        json.put("Название", elements.text().replace("Аудио", ""));
        int x = 1;
        while(true){
            elements = document.select(".biblio_book_info_detailed_left li:nth-child(" + x + ")");
            if(elements.size() == 0)
                break;
            String[] temp = elements.text().split(":");
            if(temp[0].equals("Дата написания") && temp[1].contains(",")){
                temp[1] = temp[1].substring(0, temp[1].indexOf(","));
            }
            json.put(temp[0], temp[1]);
            x++;
        }
        elements = document.select("#unr_buynow > form > button > span.simple-price");
        String price = elements.text();
        if(price.contains(" ")) {
            price = price.substring(0, price.indexOf(" "));
            if (price.equals("")) {
                price = "0";
            }
        }
        json.put("Цена", price);
        elements = document.select("#is_book_tab > div:nth-child(1) > div > div > div > div.biblio_book_descr > div.biblio_book_descr_publishers > p:nth-child(1)");
        json.put("Описание", elements.text());
        try(FileWriter writer = new FileWriter("scrappedData/data.txt", true))
        {
            writer.write(json.toString());
            writer.write("\n");
            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void main(String[] args) {
        scanItems();
    }
}
