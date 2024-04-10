package com.example.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Bean
    @StepScope
    public Resource resource(@Value("#{jobParameters['fileName']}") final String fileName) {
        return new ClassPathResource(fileName);
    }

    @Bean
    public ItemReader<Person> reader(Resource resource) {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(resource)
                .delimited()
                .names("firstName", "lastName")
                .targetType(Person.class)
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<Person> reader,
                      ItemProcessor<Person, Person> processor,
                      ItemWriter<Person> writer,
                      TaskExecutor simpleAsyncTaskExecutor) {
        return new StepBuilder("step1", jobRepository)
                .<Person, Person> chunk(3, transactionManager)
                .faultTolerant()
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public TaskExecutor simpleAsyncTaskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository, TaskExecutor simpleAsyncTaskExecutor) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
        return jobLauncher;
    }
}
