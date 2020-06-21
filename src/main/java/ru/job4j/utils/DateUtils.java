package ru.job4j.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DateUtils {

    private final static Map<String, Integer> MONTH_MAPPER = new HashMap<>() {
        {
            put("янв", 1);
            put("фев", 2);
            put("мар", 3);
            put("апр", 4);
            put("май", 5);
            put("июн", 6);
            put("июл", 7);
            put("авг", 8);
            put("сен", 9);
            put("окт", 10);
            put("ноя", 11);
            put("дек", 12);
        }
    };

    private final static Map<String, BiFunction<String, String, LocalDateTime>> DISPATCHER = Map.of(
            "сегодня", DateUtils::parseToday,
            "вчера", DateUtils::parseYesterday
    );

    public static Timestamp parse(String date) {
        String[] parts = date.split(", ");
        return Timestamp.valueOf(
                DISPATCHER.getOrDefault(parts[0], DateUtils::parseNormal).apply(parts[0], parts[1])
        );
    }

    private static LocalDateTime parseToday(String date, String time) {
        return LocalDateTime.of(
                LocalDate.now(),
                parseTime(time)
        );
    }

    private static LocalDateTime parseYesterday(String date, String time) {
        return LocalDateTime.of(
                LocalDate.now().minusDays(1),
                parseTime(time)
        );
    }

    private static LocalDateTime parseNormal(String date, String time) {
        String[] parts = date.split(" ");
        int day = Integer.parseInt(parts[0]);
        int month = MONTH_MAPPER.get(parts[1]);
        int year = 2000 + Integer.parseInt(parts[2]);
        return LocalDateTime.of(
                LocalDate.of(
                        year, month, day
                ),
                parseTime(time)
        );
    }

    private static LocalTime parseTime(String time) {
        String[] parts = time.split(":");
        return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

}
