package ru.litres;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

public class SynonymAnalyzer extends Analyzer {
    private SynonymMap synonymMap;

    public SynonymAnalyzer(SynonymMap synonymMap) {
        this.synonymMap = synonymMap;
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokenStream = new SynonymGraphFilter(tokenizer, synonymMap, true);
        System.out.println(tokenStream);
        Analyzer.TokenStreamComponents analyzer = null;
        try {
            tokenStream = new FlattenGraphFilter(tokenStream);

            analyzer = new Analyzer.TokenStreamComponents(tokenizer, tokenStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return analyzer;
    }
}
