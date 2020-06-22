package ru.job4j.grabber;

import com.sun.net.httpserver.HttpServer;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.db.PsqlStore;
import ru.job4j.db.Store;
import ru.job4j.html.Parse;
import ru.job4j.html.SqlRuParse;
import ru.job4j.model.Post;
import ru.job4j.quartz.ConfigManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
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

    public void web(Store store) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 9000), 0);
            server.createContext("/vacancies", exchange -> {
                List<Post> posts = store.findAll();
                StringJoiner html = new StringJoiner(System.lineSeparator());
                html.add("<!DOCTYPE html>");
                html.add("<html>");
                html.add("<head>");
                html.add("<meta charset=\"UTF-8\">");
                html.add("<title>Vacancies</title>");
                html.add("</head>");
                html.add("<body>");

                html.add("<table style=\"border: 1px solid black;\">");
                html.add("<tr style=\"border: 1px solid black;\">");
                html.add("<th style=\"border: 1px solid black;\">Name</th>");
                html.add("<th style=\"border: 1px solid black;\">Date</th>");
                html.add("<th style=\"border: 1px solid black;\">Description</th>");
                html.add("</tr>");

                for (Post post : posts) {
                    html.add("<tr style=\"border: 1px solid black;\">");
                    html.add(String.format("<td style=\"border: 1px solid black;\"><a href=\"%s\">%s</a></td>", post.getLink(), post.getLinkText()));
                    html.add(String.format("<td style=\"border: 1px solid black;\">%s</td>", post.getCreateDate()));
                    html.add(String.format("<td style=\"border: 1px solid black;\">%s</td>", post.getDescription()));
                    html.add("</tr>");
                }

                html.add("</table>");

                html.add("</body>");
                html.add("</html>");

                byte[] bytes = html.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().put("Content-Type", List.of("text/html", "charset=UTF-8"));
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                    os.flush();
                }
            });
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(), store, scheduler);
        grab.web(store);
    }
}