package com.github.keithwoelke.matchers.html;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import javax.xml.transform.Source;
import java.util.List;

@SuppressWarnings("unused")
public class HtmlUnescapingMatcher<T> extends BaseMatcher<T> {

    private String expected;

    HtmlUnescapingMatcher(String expected) {
        this.expected = StringEscapeUtils.unescapeHtml4(expected);
    }

    public HtmlUnescapingMatcher(List<Source> schemaFiles) {
    }

    public static HtmlUnescapingMatcher equalToUnescapingHtml4(String html) {
        return new HtmlUnescapingMatcher(html);
    }

    public boolean matches(Object o) {
        String actual = (String) o;

        return StringEscapeUtils.unescapeHtml4(actual).equals(expected);
    }

    public void describeTo(Description description) {
        description.appendValue(this.expected);
    }
}
