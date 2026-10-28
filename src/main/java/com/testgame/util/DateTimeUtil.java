package com.testgame.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class DateTimeUtil {

    private final static String UTC = "UTC";

    public static LocalDate todayUtc() {
        return toUtcDate(Instant.now());
    }

    public static LocalDate toUtcDate(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return instant.atZone(ZoneId.of(UTC)).toLocalDate();
    }

    public static Instant startOfDay(LocalDate day) {
        Objects.requireNonNull(day, "day must not be null");
        return day.atStartOfDay(ZoneId.of(UTC)).toInstant();
    }

    public static Instant startOfNextDay(LocalDate day) {
        Objects.requireNonNull(day, "day must not be null");
        return startOfDay(day.plusDays(1));
    }
}
