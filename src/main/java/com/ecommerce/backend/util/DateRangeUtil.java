package com.ecommerce.backend.util;

import com.ecommerce.backend.enums.DateRange;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateRangeUtil {

    public static DateRangeResult getRange(DateRange range) {

        LocalDate today = LocalDate.now();

        LocalDate start;
        LocalDate end;

        switch (range) {

            case TODAY -> {
                start = today;
                end = today;
            }

            case YESTERDAY -> {
                start = today.minusDays(1);
                end = today.minusDays(1);
            }

            case LAST_7_DAYS -> {
                start = today.minusDays(6);
                end = today;
            }

            case THIS_MONTH -> {
                start = today.withDayOfMonth(1);
                end = today;
            }

            case LAST_MONTH -> {
                LocalDate lastMonth = today.minusMonths(1);
                start = lastMonth.withDayOfMonth(1);
                end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
            }

            case LAST_3_MONTHS -> {
                start = today.minusMonths(3).withDayOfMonth(1);
                end = today;
            }

            case LAST_6_MONTHS -> {
                start = today.minusMonths(6).withDayOfMonth(1);
                end = today;
            }

            default -> {
                start = today.withDayOfYear(1);
                end = today;
            }
        }

        return new DateRangeResult(
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX)
        );
    }
}