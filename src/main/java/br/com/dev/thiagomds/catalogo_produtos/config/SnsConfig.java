package br.com.dev.thiagomds.catalogo_produtos.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class SnsConfig {

    @Value("aws.region")
    private String awsRegion;

    @Value("$aws.sns.topic.product.events.arn")
    private String productEventsTopic;

    // SNS Client para permitir acesso ao tópico
    @Bean
    public AmazonSNS snsClient(){
        return AmazonSNSClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    // Nome do Tópico através na qual queremos publicar evento
    @Bean(name = "productEventsTopic")
    public Topic snsProductEventsTopic(){
        return new Topic().withTopicArn(productEventsTopic);
    }
}
