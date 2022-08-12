package ru.aakhm.inflationrest.utils;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateTimeUtil {
    @Getter
    @ToString
    public static class TwoDateTimeEvents {
        private final LocalDateTime localDateTime11;
        private final LocalDateTime localDateTime12;
        private final LocalDateTime localDateTime21;
        private final LocalDateTime localDateTime22;

        private TwoDateTimeEvents(LocalDateTime localDateTime11, LocalDateTime localDateTime12, LocalDateTime localDateTime21, LocalDateTime localDateTime22) {
            this.localDateTime11 = localDateTime11;
            this.localDateTime12 = localDateTime12;
            this.localDateTime21 = localDateTime21;
            this.localDateTime22 = localDateTime22;
        }

        public static TwoDateTimeEvents of(String date1, String date2) {
            return new TwoDateTimeEvents(
                    DateTimeUtil.parseAsFirstDayOfMonth(date1),
                    DateTimeUtil.parseAsLastDayOfMonth(date1),
                    DateTimeUtil.parseAsFirstDayOfMonth(date2),
                    DateTimeUtil.parseAsLastDayOfMonth(date2));
        }

        public static TwoDateTimeEvents of(LocalDate date1, LocalDate date2) {
            return new TwoDateTimeEvents(
                    DateTimeUtil.atFirstDayOfMonth(date1),
                    DateTimeUtil.atLastDayOfMonth(date1),
                    DateTimeUtil.atFirstDayOfMonth(date2),
                    DateTimeUtil.atLastDayOfMonth(date2));
        }

    }

    public static LocalDateTime parseAsFirstDayOfMonth(String date) {
        return LocalDate.parse(date).with(TemporalAdjusters.firstDayOfMonth()).atTime(LocalTime.MIN);
    }

    public static LocalDateTime atFirstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth()).atTime(LocalTime.MIN);
    }

    public static LocalDateTime parseAsLastDayOfMonth(String date) {
        return LocalDate.parse(date).with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
    }

    public static LocalDateTime atLastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
    }

    public static Date dateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}