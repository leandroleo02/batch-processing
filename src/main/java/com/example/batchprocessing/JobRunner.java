package com.example.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobRunner implements CommandLineRunner {
    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher;

    public JobRunner(JobRegistry jobRegistry, JobLauncher jobLauncher) {
        this.jobRegistry = jobRegistry;
        this.jobLauncher = jobLauncher;
    }

    @Override
    public void run(String... args) throws Exception {
        final var jobParameters = new JobParametersBuilder()
                .addString("batchId", UUID.randomUUID().toString())
                .addString("fileName", "sample-data.csv", false)
                .toJobParameters();

        Job job = jobRegistry.getJob("importUserJob");

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        System.out.println("Job Exit Status : " + jobExecution.getStatus());
    }
}