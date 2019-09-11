package com.edutec.moodleXapiTransformer;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * supplies conversion for dates
 */
@Service
public class DateService {

    public LocalDate convertToLocalDate(Long unixtime) {
        return Instant.ofEpochMilli(unixtime).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public DateTime convertToDateTime(Long unixtime) {
        Date date = new Date(unixtime * 1000L);
        return new DateTime(date);
    }

    public DateTime now() {
        return DateTime.now();
    }
}
