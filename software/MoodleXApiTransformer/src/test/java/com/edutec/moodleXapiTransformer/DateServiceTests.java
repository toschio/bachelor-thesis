package com.edutec.moodleXapiTransformer;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class DateServiceTests {

    private DateService dateService = new DateService();


    @Test
    public void shouldConvertIntoDatetime() throws Exception {
        Long l = 1416668401L;
//        Mon Jul 29 10:08:19 2019 UTC
        DateTime dateTime = DateTime.parse("2014-11-22T16:00:1.000+00:00");

        DateTime result = dateService.convertToDateTime(l);
        Assert.assertNotNull(result);
        Assert.assertEquals(dateTime.getYear(), result.getYear());
        Assert.assertEquals(dateTime.getMonthOfYear(), result.getMonthOfYear());
        Assert.assertEquals(dateTime.getDayOfMonth(), result.getDayOfMonth());
        Assert.assertEquals(dateTime.getHourOfDay(), result.getHourOfDay());
        Assert.assertEquals(dateTime.getMinuteOfHour(), result.getMinuteOfHour());
    }
}
