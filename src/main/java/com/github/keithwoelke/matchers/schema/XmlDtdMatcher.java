package com.github.keithwoelke.matchers.schema;

import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.StringJoiner;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public class XmlDtdMatcher extends TypeSafeMatcher<String> {

    private static final Logger logger = LoggerFactory.getLogger(XmlDtdMatcher.class);

    private final InputStream dtd;
    private List<Exception> exceptions = Lists.newArrayList();

    public XmlDtdMatcher(InputStream dtd) {
        this.dtd = dtd;
    }

    public static XmlDtdMatcher matchesDtdInClasspath(String dtd) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(dtd);

        return new XmlDtdMatcher(inputStream);
    }

    public static String stripDoctype(String xml) throws XMLStreamException {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        XMLInputFactory inFactory = XMLInputFactory.newFactory();
        XMLOutputFactory outFactory = XMLOutputFactory.newFactory();

        XMLEventReader input = inFactory.createXMLEventReader(inputStream);
        XMLEventReader filtered = inFactory.createFilteredReader(input, new DTDFilter());
        XMLEventWriter output = outFactory.createXMLEventWriter(outputStream);

        output.add(filtered);
        output.flush();
        output.close();

        return new String(outputStream.toByteArray());
    }

    public boolean matchesSafely(String item) {
        File file = new File("");

        try {
            String removedDoctype = stripDoctype(item);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();

            //parse file into DOM
            Document doc = db.parse(new ByteArrayInputStream(removedDoctype.getBytes()));
            DOMSource source = new DOMSource(doc);

            //now use a transformer to add the DTD element
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            file = writeToTempFile();

            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, file.getPath());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            factory.setValidating(true);
            db = factory.newDocumentBuilder();
            db.parse(new InputSource(new StringReader(writer.toString())));
        } catch (IOException | TransformerException | SAXException | ParserConfigurationException | XMLStreamException e) {
            logger.error("", e);
            exceptions.add(e);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return exceptions.isEmpty();
    }

    private File writeToTempFile() throws IOException {
        //write the inputStream to a FileOutputStream
        File file = File.createTempFile("tempFile", "xml");
        file.deleteOnExit();
        OutputStream out = new FileOutputStream(file);

        int read;
        byte[] bytes = new byte[1024];

        while ((read = dtd.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }

        out.flush();
        out.close();

        return file;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("document with valid valid schema");
    }

    @Override
    public void describeMismatchSafely(String o, Description description) {
            StringJoiner joiner = new StringJoiner("\n");

            exceptions.forEach(e -> joiner.add(e.getMessage()));

            description.appendText(joiner.toString());
    }

    @SuppressWarnings("unused")
    static class DTDFilter implements EventFilter
    {
        @Override
        public boolean accept(XMLEvent event) {
            return event.getEventType() != XMLStreamConstants.DTD;
        }

    }
}