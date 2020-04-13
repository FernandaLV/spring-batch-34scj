package br.com.fiap.librarybatchchunk;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
@EnableBatchProcessing
public class LibraryBatchChunkApplication {
	
	@Bean
	public SkipPolicy fileVerificationSkipper() {
		return new FileVerificationSkipper();
	}
	
	@Bean
    public FlatFileItemReader<Pessoa> itemReader(@Value("${file.input}") Resource resource) {
    	return new FlatFileItemReaderBuilder<Pessoa>()
                .name("file item reader")
                .targetType(Pessoa.class)
                .fixedLength()
                .addColumns(new Range(1,41))
                .addColumns(new Range(42,53))
                .names("nome", "cpf")
                .resource(resource)
                .build();
    }

    @Bean
    public ItemProcessor<Pessoa, Pessoa> itemProcessor() {
        return pessoa -> {
        	if (pessoa.getNome() == "") {
        		return null;
        	}
        	
            pessoa.setNome(pessoa.getNome().toUpperCase());
            pessoa.setCpf(
                    pessoa.getCpf()
                            .replaceAll("\\.", "")
                            .replace("-", "")
            );
            return pessoa;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Pessoa> itemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Pessoa>()
                .dataSource(dataSource)
                .sql("insert into TB_PESSOA (nome, cpf) values (:nome, :cpf)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory,
                     ItemReader<Pessoa> itemReader,
                     ItemProcessor<Pessoa, Pessoa> itemProcessor,
                     ItemWriter<Pessoa> itemWriter) {
        return stepBuilderFactory.get("pessoa step")
                .<Pessoa, Pessoa>chunk(2)
                .reader(itemReader).faultTolerant().skipPolicy(fileVerificationSkipper())
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory,
                   Step step) {
        return jobBuilderFactory.get("processar pessoa job")
                .start(step)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(LibraryBatchChunkApplication.class, args);
    }

}
