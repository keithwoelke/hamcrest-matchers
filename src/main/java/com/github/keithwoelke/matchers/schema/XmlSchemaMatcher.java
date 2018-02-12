package com.github.keithwoelke.matchers.schema;

import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "CanBeFinal"})
public class XmlSchemaMatcher<T> extends BaseMatcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(XmlSchemaMatcher.class);

    protected final List<Source> schemaFiles;
    private List<Exception> exceptions = Lists.newArrayList();

    XmlSchemaMatcher() {
        InputStream dctermsSchema = XmlSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/dcterms.xsd");
        InputStream dcSchema = XmlSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/dc.xsd");
        InputStream xmlSchema = XmlSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/xml.xsd");
        InputStream dcmitypeSchema = XmlSchemaMatcher.class.getClassLoader().getResourceAsStream("schema/dcmitype.xsd");

        Source dcterms = new StreamSource(dctermsSchema);
        Source dc = new StreamSource(dcSchema);
        Source xml = new StreamSource(xmlSchema);
        Source dcmitype = new StreamSource(dcmitypeSchema);

        this.schemaFiles = Lists.newArrayList(dcmitype, xml, dc, dcterms);
    }

    public XmlSchemaMatcher(List<Source> schemaFiles) {
        this.schemaFiles = schemaFiles;
    }

    public XmlSchemaMatcher(Source[] schemaFiles) {
        this(Lists.newArrayList(schemaFiles));
    }

    public static XmlSchemaMatcher matchesXsd() {
        return new XmlSchemaMatcher();
    }

    public static XmlSchemaMatcher matchesXsd(Source... schemaFiles) {
        return new XmlSchemaMatcher(schemaFiles);
    }

    public static XmlSchemaMatcher matchesXsdInClasspath(String... schemaFiles) {
        List<StreamSource> streamSources = Arrays.stream(schemaFiles)
                .map(XmlSchemaMatcher.class.getClassLoader()::getResourceAsStream)
                .map(StreamSource::new)
                .collect(Collectors.toList());

        return new XmlSchemaMatcher(streamSources);
    }

    @Override
    public boolean matches(Object o) {
        StreamSource streamSource = new StreamSource();

        if (o == null || !(o instanceof String || o instanceof Source))  {
            return false;
        }

        try {
            if (o instanceof String) {
                String xml = XmlDtdMatcher.stripDoctype((String) o);
                InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
                streamSource = new StreamSource(inputStream);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


            Schema schema = schemaFactory.newSchema(schemaFiles.toArray(new Source[]{}));
            Validator validator = schema.newValidator();
            validator.validate(streamSource);
        } catch (SAXException | IOException | XMLStreamException e) {
            logger.error("", e);
            exceptions.add(e);
        }

        return exceptions.isEmpty();
    }

    public void describeTo(Description description) {
        description.appendText("document with valid schema");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void describeMismatch(Object o, Description description) {
        if (o == null) {
            super.describeMismatch(o, description);
        } else if (!(o instanceof String || o instanceof Source)) {
            description.appendText("was a ")
                    .appendText(o.getClass().getName())
                    .appendText(" (")
                    .appendValue(o)
                    .appendText(")");
        } else {
            StringJoiner joiner = new StringJoiner("\n");

            exceptions.forEach(e -> joiner.add(e.getMessage()));

            description.appendText(joiner.toString());
        }
    }
}
