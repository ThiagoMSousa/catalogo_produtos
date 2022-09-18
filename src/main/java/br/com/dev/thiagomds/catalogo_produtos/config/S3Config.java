package br.com.dev.thiagomds.catalogo_produtos.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class S3Config {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder.standard()
                // Definindo Região
                .withRegion(awsRegion)
                // Definindo a Fonte de Credenciais de Acesso
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
}
