package com.github.keithwoelke.matchers.date;

import org.exparity.hamcrest.date.core.TemporalFormatter;
import org.exparity.hamcrest.date.core.TemporalWrapper;
import org.exparity.hamcrest.date.core.format.ZonedDateTimeFormatter;
import org.exparity.hamcrest.date.core.wrapper.ZonedDateTimeWrapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("unused")
public class StringDateTimeMatchers extends TypeSafeMatcher<String> {

    public static Matcher<String> within(long period, ChronoUnit unit, ZonedDateTime date) {
        return new IsWithin(period, unit, new ZonedDateTimeWrapper(date), new ZonedDateTimeFormatter());
    }

    public static Matcher<String> within(long period, ChronoUnit unit, String date) {
        DateTime dt = DateTime.parse(date);

        ZonedDateTime zdt = ZonedDateTime.of(
                dt.getYear(),
                dt.getMonthOfYear(),
                dt.getDayOfMonth(),
                dt.getHourOfDay(),
                dt.getMinuteOfHour(),
                dt.getSecondOfMinute(),
                dt.getMillisOfSecond() * 1_000_000,
                ZoneId.of(dt.getZone().getID(), ZoneId.SHORT_IDS));

        return new IsWithin(period, unit, new ZonedDateTimeWrapper(zdt), new ZonedDateTimeFormatter());
    }

    @Override
    protected boolean matchesSafely(String item) {
        return false;
    }

    @Override
    public void describeTo(Description description) {

    }

    @SuppressWarnings("unused")
    public static class IsWithin extends TypeSafeDiagnosingMatcher<String> {
        private final long period;
        private final ChronoUnit unit;
        private final TemporalWrapper<ZonedDateTime> expected;
        private final TemporalFormatter<ZonedDateTime> describer;

        public IsWithin(long period, ChronoUnit unit, TemporalWrapper<ZonedDateTime> expected, TemporalFormatter<ZonedDateTime> describer) {
            this.period = period;
            this.unit = unit;
            this.expected = expected;
            this.describer = describer;
        }

        protected boolean matchesSafely(String actual, Description mismatchDesc) {
            DateTime dt = DateTime.parse(actual);

            ZonedDateTime zdt = ZonedDateTime.of(
                    dt.getYear(),
                    dt.getMonthOfYear(),
                    dt.getDayOfMonth(),
                    dt.getHourOfDay(),
                    dt.getMinuteOfHour(),
                    dt.getSecondOfMinute(),
                    dt.getMillisOfSecond() * 1_000_000,
                    ZoneId.of(dt.getZone().getID(), ZoneId.SHORT_IDS));

            long actualDuration = this.expected.difference(zdt, this.unit);
            if(actualDuration > this.period) {
                mismatchDesc.appendText("the date is " + this.describer.describe(zdt) + " and " + actualDuration + " " + this.describeUnit() + " different");
                return false;
            } else {
                return true;
            }
        }

        public void describeTo(Description description) {
            description.appendText("the date is within " + this.period + " " + this.describeUnit() + " of " + this.describer.describe(this.expected.unwrap()));
        }

        private java.lang.String describeUnit() {
            return this.unit.toString().toLowerCase();
        }
    }
}
