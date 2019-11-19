package ru.litres;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Indexer {
    public static Directory fileDirectory;
    public static Analyzer analyzer;
    private static Path pathDirectory = Paths.get("filesIndex");
    private static File pathFile = new File("temp");

    private static File dataDir;

    public Indexer(SimpleFSDirectory fileDirectory, Analyzer analyzer, File dataDir) {
        this.fileDirectory = fileDirectory;
        this.analyzer = analyzer;
        this.dataDir = dataDir;
    }

    public void createIndex(Set<String> textFieldsStore, Set<String> intPoints,  Set<String> storedFields) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(fileDirectory, indexWriterConfig);

        File[] files = dataDir.listFiles();
        for (File file : files) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

                while (bufferedReader.ready()) {
                    JSONObject book = new JSONObject(bufferedReader.readLine());

                    Document document = new Document();

                    textFieldsStore.stream().forEach(textField -> document.add(new TextField(textField, book.getString(textField), Field.Store.YES)));

                    intPoints.stream().forEach(intPoint -> document.add(new IntPoint(intPoint, new Scanner(book.getString(intPoint)).nextInt())));

                    storedFields.stream().forEach(storedField -> document.add(new StoredField(storedField, book.getString(storedField))));
                    writer.addDocument(document);
                }
            }

            System.out.println(file.getName() + " indexed");
        }
        writer.close();
    }

    public List<Document> searchIndex(Query query, int maxSize) throws IOException {
        IndexReader indexReader = DirectoryReader.open(fileDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        TopDocs topDocs = indexSearcher.search(query, maxSize);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(indexSearcher.doc(scoreDoc.doc));
        }

        return documents;
    }

    public static void createBookIndex() throws IOException {
        SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(pathDirectory);
        Indexer indexer = new Indexer(simpleFSDirectory, new StandardAnalyzer(), pathFile);

        Set textFields = Set.of("Название", "Описание");
        Set intFields = Set.of("Цена");
        Set otherFields = Set.of("Длительность", "Возрастное ограничение");

        indexer.createIndex(textFields, intFields, otherFields);
    }

    public static void searchBooksByPrice() throws IOException {
        SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(pathDirectory);
        Indexer indexer = new Indexer(simpleFSDirectory, new StandardAnalyzer(), pathFile);

        Query query =  IntPoint.newRangeQuery("Цена",200,300);

        List<Document> documents = indexer.searchIndex(query, 32);
        for (Document document: documents) {
            System.out.println(document.toString());
        }

        System.out.println("Found: " + documents.size() + " books");
    }

    public static void searchBooksByName() throws IOException {
        SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(pathDirectory);
        Indexer indexer = new Indexer(simpleFSDirectory, new StandardAnalyzer(), pathFile);

        Term term = new Term("Описание", "лучших");
        Query query = new TermQuery(term);

        List<Document> documents = indexer.searchIndex(query, 8);
        for (Document document: documents) {
            System.out.println(document.toString());
        }
        System.out.println("Found: " + documents.size() + " books");
    }

    public static void main(String[] args) throws IOException {
        createBookIndex();
        searchBooksByPrice();
        searchBooksByName();
    }
}
