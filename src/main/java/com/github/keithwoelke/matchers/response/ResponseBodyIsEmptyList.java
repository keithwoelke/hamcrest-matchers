package com.github.keithwoelke.matchers.response;

import io.restassured.response.ResponseBody;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

@SuppressWarnings("unused")
public class ResponseBodyIsEmptyList extends TypeSafeMatcher<ResponseBody> {

    @Override
    public void describeTo(Description description) {
        description.appendText("was not empty");
    }

    @Override
    protected boolean matchesSafely(ResponseBody response) {
        return response.jsonPath().getList("$").isEmpty();
    }

    public static Matcher<ResponseBody> isEmptyList() {
        return new ResponseBodyIsEmptyList();
    }
}
