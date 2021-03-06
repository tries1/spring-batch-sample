package com.spring.smaple.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job simpleJob() {
        // Job은 하나의 배치 작업 단위
        return jobBuilderFactory.get("simpleJob")// job의 이름은 별도로 지정하지 않고, 이렇게 Builder를 통해 지정합니다.
                .start(simpleStep1(null))
                .next(simpleStep2(null))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("simpleStep1")// jobBuilderFactory.get("simpleJob")와 마찬가지로 Builder를 통해 이름을 지정합니다.
                .tasklet((contribution, chunkContext) -> {
                    //Step 안에서 수행될 기능들을 명시합니다.
                    //Tasklet은 Step안에서 단일로 수행될 커스텀한 기능들을 선언할때 사용합니다.
                    //여기서는 Batch가 수행되면 log.info(">>>>> This is Step1") 가 출력되도록 합니다.
                    log.info(">>>>>>>> This is Step1");
                    log.info(">>>>>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        //Job 안에는 여러 Step이 존재하고, Step 안에 Tasklet 혹은 Reader & Processor & Writer 묶음이 존재
        return stepBuilderFactory.get("simpleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>>> This is Step2");
                    log.info(">>>>>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
