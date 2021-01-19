package com.spring.smaple.batch.job;

import com.spring.smaple.batch.entity.User;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserJdbcCursorItemReaderJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int chunkSize = 10;

    @Bean
    public Job userJdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("userJdbcCursorItemReaderJob")
                .start(userJdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step userJdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("userJdbcCursorItemReaderStep")
                // 첫번째 Pay는 Reader에서 반환할 타입이며, 두번째 Pay는 Writer에 파라미터로 넘어올 타입
                // chunkSize 인자값을 넣은 경우는 Reader & Writer가 묶일 Chunk 트랜잭션 범위입니다.
                .<User, User>chunk(chunkSize)
                .reader(userJdbcCursorItemReader())
                .writer(userJdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<User> userJdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<User>()
                // Paging과는 다른 것이, Paging은 실제 쿼리를 limit, offset을 이용해서 분할 처리하는 반면,
                // Cursor는 쿼리는 분할 처리 없이 실행되나 내부적으로 가져오는 데이터는 FetchSize만큼 가져와 read()를 통해서 하나씩 가져옵니다.
                .fetchSize(chunkSize) // Database에서 한번에 가져올 데이터 양을 나타냅니다.
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(User.class))
                .sql("select id, name from users")
                .name("userJdbcCursorItemReader") // reader의 이름을 지정합니다., Bean의 이름이 아니며 Spring Batch의 ExecutionContext에서 저장되어질 이름
                .build();
    }

    private ItemWriter<User> userJdbcCursorItemWriter() {
        return list -> {
            for (User user : list) {
                log.info("Current user={}", user);
            }
        };
    }
}
