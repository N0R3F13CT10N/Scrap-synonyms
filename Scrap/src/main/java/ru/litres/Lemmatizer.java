package ru.litres;

import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Lemmatizer {
    private final static MyStem mystemAnalyzer =
            new Factory("-igd --eng-gr --format json --weight")
                    .newMyStem("3.0", Option.<File>empty()).get();

    public String getLemmatization(String string) throws MyStemApplicationException {
        final List<Info> result =
                JavaConversions.seqAsJavaList(
                        mystemAnalyzer.analyze(Request.apply(string)).info().toList());

        return result.stream().map(e->e.lex().get()).collect(Collectors.joining(", "));
    }
}
