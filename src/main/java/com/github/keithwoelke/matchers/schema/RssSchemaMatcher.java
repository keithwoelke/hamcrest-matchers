package com.github.keithwoelke.matchers.schema;

import org.hamcrest.Matcher;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

@SuppressWarnings("unused")
public class RssSchemaMatcher<T> extends XmlSchemaMatcher<T> {

    public RssSchemaMatcher() {
        super();

        InputStream rssSchema = MrssSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/rss.xsd");
        Source rss = new StreamSource(rssSchema);

        this.schemaFiles.add(rss);
    }

    public static <T> Matcher<T> matchesRssXsd() {
        return new RssSchemaMatcher<>();
    }
}
