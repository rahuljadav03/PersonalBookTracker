package com.example.booktracker.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class DateTimeUtilsTest {

    private TimeZone originalTimeZone;

    @Before
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    public void testFormatIst_KnownUtcToIstConversion_AmTime() {
        // Change device default timezone to America/New_York to prove IST independence
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

        // 1700000000000L = Nov 14, 2023 22:13:20 UTC -> IST = UTC + 5:30 -> Nov 15, 2023, 03:43 AM
        String formatted = DateTimeUtils.formatIst(1700000000000L);
        assertEquals("15 Nov 2023, 03:43 AM", formatted);
    }

    @Test
    public void testFormatIst_PmTime() {
        // Change device default timezone to UTC to prove IST independence
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // 1700060000000L = Nov 15, 2023 14:53:20 UTC -> IST = UTC + 5:30 -> Nov 15, 2023, 08:23 PM
        String formatted = DateTimeUtils.formatIst(1700060000000L);
        assertEquals("15 Nov 2023, 08:23 PM", formatted);
    }

    @Test
    public void testFormatIst_Midnight_1200AM() {
        // 1700073000000L = Nov 15, 2023 18:30:00 UTC -> IST = UTC + 5:30 -> Nov 16, 2023, 12:00 AM
        String formatted = DateTimeUtils.formatIst(1700073000000L);
        assertEquals("16 Nov 2023, 12:00 AM", formatted);
    }

    @Test
    public void testFormatIst_Noon_1200PM() {
        // 1700029800000L = Nov 15, 2023 06:30:00 UTC -> IST = UTC + 5:30 -> Nov 15, 2023, 12:00 PM
        String formatted = DateTimeUtils.formatIst(1700029800000L);
        assertEquals("15 Nov 2023, 12:00 PM", formatted);
    }

    @Test
    public void testFormatIst_ZeroAndNegativeEpochValues() {
        assertEquals("-", DateTimeUtils.formatIst(0L));
        assertEquals("-", DateTimeUtils.formatIst(-1000L));
    }
}
