package ru.job4j.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class DateUtilsTest {

    @Test
    public void whenToday() {
        LocalDateTime now = LocalDateTime.now();
        String input = String.format("сегодня, %d:%d", now.getHour(), now.getMinute());
        LocalDateTime out = DateUtils.parse(input).toLocalDateTime();
        Assert.assertEquals(now.getDayOfMonth(), out.getDayOfMonth());
        Assert.assertEquals(now.getMonthValue(), out.getMonthValue());
        Assert.assertEquals(now.getYear(), out.getYear());
        Assert.assertEquals(now.getHour(), out.getHour());
        Assert.assertEquals(now.getMinute(), out.getMinute());
    }

    @Test
    public void whenYesterday() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        String input = String.format("вчера, %d:%d", yesterday.getHour(), yesterday.getMinute());
        LocalDateTime out = DateUtils.parse(input).toLocalDateTime();
        Assert.assertEquals(yesterday.getDayOfMonth(), out.getDayOfMonth());
        Assert.assertEquals(yesterday.getMonthValue(), out.getMonthValue());
        Assert.assertEquals(yesterday.getYear(), out.getYear());
        Assert.assertEquals(yesterday.getHour(), out.getHour());
        Assert.assertEquals(yesterday.getMinute(), out.getMinute());
    }

    @Test
    public void whenNormal() {
        LocalDateTime expect = LocalDateTime.of(2020, 5, 18, 11, 12);
        String input = String.format("18 май 20, %d:%d", expect.getHour(), expect.getMinute());
        LocalDateTime out = DateUtils.parse(input).toLocalDateTime();
        Assert.assertEquals(expect.getDayOfMonth(), out.getDayOfMonth());
        Assert.assertEquals(expect.getMonthValue(), out.getMonthValue());
        Assert.assertEquals(expect.getYear(), out.getYear());
        Assert.assertEquals(expect.getHour(), out.getHour());
        Assert.assertEquals(expect.getMinute(), out.getMinute());
    }

}