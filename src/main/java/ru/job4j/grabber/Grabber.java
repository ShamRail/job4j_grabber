package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.db.PsqlStore;
import ru.job4j.db.Store;
import ru.job4j.html.Parse;
import ru.job4j.html.SqlRuParse;
import ru.job4j.model.Post;
import ru.job4j.quartz.ConfigManager;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private ConfigManager cfg;

    public Store store() throws IOException {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        cfg = new ConfigManager("app.properties");
    }

    private Predicate<String> searchCondition() {
        return name -> {
            String[] include = cfg.get("language-include").split(",");
            String[] exclude = cfg.get("language-exclude").split(",");
            String lowerName = name.toLowerCase();
            boolean result = true;
            for (String in : include) {
                if (!lowerName.contains(in)) {
                    result = false;
                    break;
                }
            }
            if (result) {
                for (String ex : exclude) {
                    if (lowerName.contains(ex)) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        };
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        data.put("condition", searchCondition());
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.get("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            Predicate<String> condition = (Predicate<String>) map.get("condition");

            Timestamp lastDate = store.lastItem();
            Predicate<Timestamp> until = (date) -> date.before(lastDate);
            List<Post> posts = new LinkedList<>();

            for (String resource : parse.resources()) {
                List<Post> urlPosts = parse.list(resource, until);
                if (urlPosts.isEmpty()) {
                    break;
                }
                posts.addAll(urlPosts.stream()
                        .filter(p -> condition.test(p.getLinkText()))
                        .collect(Collectors.toList()));
            }
            posts.forEach(p -> p.setDescription(parse.detail(p.getLink()).getDescription()));
            store.saveAll(posts);
        }
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(), store, scheduler);
    }
}