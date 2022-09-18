package br.com.dev.thiagomds.catalogo_produtos.config.local;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class S3ConfigLocal {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConfigLocal.class);

    // Nome do Bucket
    private static final String BUCKET_NAME = "pcs-invoice";

    private AmazonS3 amazonS3; // Representa o Client do S3

    public S3ConfigLocal() {
        // Criando o cliente do S3
        amazonS3 = getAmazonS3();

        // Criando o Bucket
        createBucket();

        // Criando o Client do SNS
        AmazonSNS snsClient = getAmazonSNS();

        // Criando o tópico
        String s3InvoiceEventsTopicArn = createTopic(snsClient);

        // Pegando o client do AmazonSQS
        AmazonSQS sqsClient = getAmazonSQS();

        // Criando a Fila
        createQueue(snsClient, s3InvoiceEventsTopicArn, sqsClient);

        // Configurando o Bucket
        // Informando que o Tópico é a fonte do evento quando inserido o objeto
        // Apontando o Bucket para o Tópico
        configueBucket(s3InvoiceEventsTopicArn);
    }

    private AmazonS3 getAmazonS3() {

        // Definindo Credenciais de Acesso
        AWSCredentials awsCredentials = new BasicAWSCredentials("test", "test");

        this.amazonS3 = AmazonS3Client.builder()
                // Definindo Região
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:4566",
                        Regions.US_EAST_1.getName()))
                // Definindo a Fonte de Credenciais de Acesso
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                // Habilitando Modo Client devido o Bucket estar sendo criado local
                .enablePathStyleAccess()
                .build();

        return amazonS3;
    }

    // Responsável por criar o BUCKET
    private void createBucket() {
        this.amazonS3.createBucket(BUCKET_NAME);
    }

    @Bean
    public AmazonS3 amazonS3Client() {
        return this.amazonS3;
    }

    private AmazonSNS getAmazonSNS() {
        return AmazonSNSClient.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:4566",
                        Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private String createTopic(AmazonSNS snsClient) {
        // Criando o Tópico
        CreateTopicRequest createTopicRequest = new CreateTopicRequest("s3-invoice-events");
        return snsClient.createTopic(createTopicRequest).getTopicArn();
    }

    private AmazonSQS getAmazonSQS() {
        return AmazonSQSClient.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:4566",
                        Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private void createQueue(AmazonSNS snsClient, String s3InvoiceEventsTopicArn, AmazonSQS sqsClient) {
        // Criando a Fila Local
        String s3InvoiceQueueUrl = sqsClient.createQueue(
                new CreateQueueRequest("s3-invoice-events")).getQueueUrl();

        // Escrevendo a Fila no Tópico
        Topics.subscribeQueue(snsClient, sqsClient, s3InvoiceEventsTopicArn, s3InvoiceQueueUrl);
    }

    private void configueBucket(String s3InvoiceEventsTopicArn) {
        // Criando a Configuração do Tópico
        TopicConfiguration topicConfiguration = new TopicConfiguration();
        // Setando o ARN do Tópico na qual deve ser Publicado
        topicConfiguration.setTopicARN(s3InvoiceEventsTopicArn);
        // Adicionando o Evento à ser Publicado no Tópico
        topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);


        // Configurando a Configuração de Notificação
        amazonS3.setBucketNotificationConfiguration(BUCKET_NAME,
                new BucketNotificationConfiguration()
                        .addConfiguration("putObject", topicConfiguration));
    }

}
