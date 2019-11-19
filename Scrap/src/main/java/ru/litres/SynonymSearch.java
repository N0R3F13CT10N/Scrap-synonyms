package ru.litres;

import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;


public class SynonymSearch {

    private SynonymMap synonymMap =  new SynonymsMapMaker().getSynonyms("data");
    private Lemmatizer lemmatizer = new Lemmatizer();

    public void findWithSynonyms(String filePath, String field, String text) {
        try {
            Directory directory = FSDirectory.open(Paths.get(filePath));
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            text = lemmatizer.getLemmatization(text);
            Query query = new QueryParser(field, new SynonymAnalyzer(synonymMap)).parse(text);
            System.out.println("Search... '" + query + "'");
            TopDocs topDocs = indexSearcher.search(query, 100);
            System.out.println(topDocs.totalHits);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Iterator<IndexableField> it = indexSearcher.doc(sd.doc).iterator();
                while (it.hasNext()) {
                    IndexableField fld = it.next();
                    System.out.println(fld.name() + " : " + fld.stringValue());
                }
            }
            indexReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SynonymSearch indexCreator = new SynonymSearch();
        Scanner in = new Scanner(System.in);
        System.out.print("Input Field:\n");
        String field = in.nextLine();
        System.out.print("\nInput text:\n");
        String text = in.nextLine();
        indexCreator.findWithSynonyms("filesIndex", field, text);
    }
}
