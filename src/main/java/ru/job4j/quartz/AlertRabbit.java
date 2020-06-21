package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        ConfigManager configManager = new ConfigManager("rabbit.properties");
        try (Connection connection = initConnection(configManager)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(dataMap)
                    .build();
            int count = Integer.parseInt(configManager.get("rabbit.interval"));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(count)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Connection initConnection(ConfigManager configManager) {
        Connection connection = null;
        try {
            Class.forName(configManager.get("driver-class-name"));
            connection = DriverManager.getConnection(
                    configManager.get("url"),
                    configManager.get("username"),
                    configManager.get("password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
                System.out.println("Rabbit runs here ...");
                System.out.println(connection.getSchema());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}