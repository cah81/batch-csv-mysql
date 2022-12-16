package com.example.batch_csv.infraestructure.job.configuration;

import com.example.batch_csv.domain.Invoice;
import com.example.batch_csv.infraestructure.job.helper.BlankLineRecordSeparatorPolicy;
import com.example.batch_csv.infraestructure.job.listener.InvoiceListener;
import com.example.batch_csv.infraestructure.repository.InvoiceRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Autowired
    InvoiceRepository repository;


    //para leer el archivo plano
    @Bean
    public FlatFileItemReader<Invoice> reader(){
        return new FlatFileItemReaderBuilder<Invoice>()
                .name("InvoiceItemReader")
                .resource(new ClassPathResource("invoices.csv"))
                .linesToSkip(1)
                .delimited()
                .names(new String[]{"name","number","amount","discount","location"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Invoice>(){{
                        setTargetType(Invoice.class);
                }})
                .build();

//        FlatFileItemReader<Invoice> reader  = new FlatFileItemReader<>();
//        reader.setResource(new ClassPathResource("/invoices.csv"));
//        reader.setLineMapper(new DefaultLineMapper<>(){{
//            setLineTokenizer(new DelimitedLineTokenizer(){{
//                setDelimiter(DELIMITER_COMMA);
//                setNames("name","number","amount","discount","location");
//            }});
//            setFieldSetMapper(new BeanWrapperFieldSetMapper<>(){{
//                setTargetType(Invoice.class);
//            }});
//        }});
//        reader.setRecordSeparatorPolicy(new BlankLineRecordSeparatorPolicy());
//        return reader;

    }
    @Bean
    public ItemWriter<Invoice> writer(){
        return invoices ->{
            System.out.println("saving invoices records " + invoices);
            repository.saveAll(invoices);
        };
    }

    public ItemProcessor<Invoice,Invoice> processor(){
        return invoice ->{
            Double discount =  invoice.getAmount()*(invoice.getDiscount()/100.0);
            Double finalAmount = invoice.getAmount()-discount;
            invoice.setFinalAmount(finalAmount);
            return invoice;
        };
    }

    @Bean
    public JobExecutionListener listener(){
        return new InvoiceListener();
    }

    @Autowired
    private StepBuilderFactory sbf;

    @Bean
    public Step stepA(){
        return sbf.get("stepA")
                .<Invoice,Invoice> chunk(2)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }


    @Autowired
    private JobBuilderFactory jbf;

    @Bean
    public Job jobA(){
        return jbf.get("jobA")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .start(stepA())
                .build();

    }






}
