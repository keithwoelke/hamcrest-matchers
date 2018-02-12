package com.github.keithwoelke.matchers.schema;

import org.hamcrest.Matcher;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

@SuppressWarnings("unused")
public class MrssSchemaMatcher<T> extends RssSchemaMatcher<T> {

    public MrssSchemaMatcher() {
        super();

        InputStream mrssSchema = MrssSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/mrss.xsd");
        Source mrss = new StreamSource(mrssSchema);

        this.schemaFiles.add(mrss);
    }

    public static <T> Matcher<T> matchesMrssXsd() {
        return new MrssSchemaMatcher<>();
    }
}
