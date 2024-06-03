package zeebe.camunda.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


@SpringBootApplication
public class WorkerApplication {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static ConsoleHandler handler = new ConsoleHandler();
    @Autowired
    private ZeebeClient client;

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    @JobWorker(type = "calculate-travel-time")
    public void calculateTravelTime(final ActivatedJob job, @Variable String startdatum, @Variable String enddatum) {
        logger.info("calculate travel time");
        final var formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withLocale(Locale.GERMANY); // 2024-06-06T12:15+02:00
        final var localStartdatum = LocalDate.parse(startdatum, formatter);
        final var localEnddatum = LocalDate.parse(enddatum, formatter);
        logger.info(localStartdatum.toString());
        logger.info(localEnddatum.toString());
        int days = localStartdatum.until(localEnddatum).getDays();
        logger.info("Tage: " + days);
        logger.info("Stunden: " + days * 24);

        client.newCompleteCommand(job.getKey())
                .variables("{\"reisedauer\": \"" + days * 24 + "\"}")
                .send()
                .exceptionally(throwable -> {
                    throw new RuntimeException("Could not complete job " + job, throwable);
                });
    }
}