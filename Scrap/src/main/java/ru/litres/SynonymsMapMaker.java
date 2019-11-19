package ru.litres;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SynonymsMapMaker {
    private final Gson gson = new Gson();

    public SynonymMap getSynonyms(String filePath) {
        SynonymMap.Builder builder = new SynonymMap.Builder();
            StringBuilder stringBuilder = new StringBuilder();

            File[] files = new File(filePath).listFiles();
            for (File file : files) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    String str;
                    while ((str = bufferedReader.readLine()) != null) {
                        stringBuilder.append(str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        Map<String, List<String>> synonyms = gson.fromJson(new String(stringBuilder),
                                                new TypeToken<Map<String, List<String>>>() {}.getType());

        for (String word : synonyms.keySet()) {
            for (String syn : synonyms.get(word)) {
                builder.add(new CharsRef(word), new CharsRef(syn), true);
                builder.add(new CharsRef(syn), new CharsRef(word), true);
            }
        }

        SynonymMap synonymMap = null;
        try {
            synonymMap = builder.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return synonymMap;
    }
}
