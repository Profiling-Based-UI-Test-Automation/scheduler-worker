package scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SchedulerAndWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerAndWorkerApplication.class, args);
    }
}