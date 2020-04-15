package br.com.fiap.librarybatchchunk;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
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
public class LibraryBatchChunkApplication extends DefaultBatchConfigurer {
	
	@Value("${spring.datasource.driverClassName}")
	private String driverClassName;
	
	@Override
    protected JobRepository createJobRepository() throws Exception {
        
		if (driverClassName.equals("org.h2.Driver")) {
			return super.createJobRepository();
		}
		
		MapJobRepositoryFactoryBean factoryBean = new MapJobRepositoryFactoryBean();
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
	
	@Bean
	public SkipPolicy fileVerificationSkipper() {
		return new FileVerificationSkipper();
	}
	
	@Bean
    public FlatFileItemReader<Aluno> itemReader(@Value("${file.input}") Resource resource) {
    	return new FlatFileItemReaderBuilder<Aluno>()
                .name("file item reader")
                .targetType(Aluno.class)
                .fixedLength()
                .addColumns(new Range(1,41))
                .addColumns(new Range(42,48))
                .addColumns(new Range(50,55))
                .names("nome", "matricula", "turma")
                .resource(resource)
                .build();
    }

    @Bean
    public ItemProcessor<Aluno, Aluno> itemProcessor() {
        return new ValidationProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Aluno> itemWriter(DataSource dataSource) {
        
    	String comandoSql = "insert into \"alunos\" (id, nome, matricula, turma) values (:id, :nome, :matricula, :turma)";
    	
		if (driverClassName.equals("org.h2.Driver")) {
			comandoSql = "insert into alunos (id, nome, matricula, turma) values (:id, :nome, :matricula, :turma)";;
		}
    	
    	return new JdbcBatchItemWriterBuilder<Aluno>()
                .dataSource(dataSource)
                .sql(comandoSql)
                .beanMapped()
                .build();
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory,
                     ItemReader<Aluno> itemReader,
                     ItemProcessor<Aluno, Aluno> itemProcessor,
                     ItemWriter<Aluno> itemWriter) {
        return stepBuilderFactory.get("pessoa step")
                .<Aluno, Aluno>chunk(2)
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
