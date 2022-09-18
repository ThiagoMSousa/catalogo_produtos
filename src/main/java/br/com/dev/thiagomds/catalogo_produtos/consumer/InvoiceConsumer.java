package br.com.dev.thiagomds.catalogo_produtos.consumer;

import br.com.dev.thiagomds.catalogo_produtos.model.Invoice;
import br.com.dev.thiagomds.catalogo_produtos.model.SnsMessage;
import br.com.dev.thiagomds.catalogo_produtos.repository.InvoiceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class InvoiceConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceConsumer.class);
    private ObjectMapper objectMapper;
    private InvoiceRepository invoiceRepository;
    private AmazonS3 amazonS3;

    @Autowired
    public InvoiceConsumer(ObjectMapper objectMapper,
                           InvoiceRepository invoiceRepository,
                           AmazonS3 amazonS3) {

        this.objectMapper = objectMapper;
        this.invoiceRepository = invoiceRepository;
        this.amazonS3 = amazonS3;
    }

    @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}")
    public void receiveS3Event(TextMessage textMessage) throws JMSException, IOException {

        // Serializando a mensagem em SnsMessage
        SnsMessage snsMessage = objectMapper.readValue(textMessage.getText(), SnsMessage.class);

        // Converte o objeto de SnsMessage para o S3EventNotification
        S3EventNotification s3EventNotification = objectMapper.readValue(
                snsMessage.getMessage(), S3EventNotification.class
        );
        processInvoiceNotification(s3EventNotification);
    }

    private void processInvoiceNotification(S3EventNotification s3EventNotification) throws IOException {

        // Varrendo todos os registros
        for (S3EventNotification.S3EventNotificationRecord
                s3EventNotificationRecord : s3EventNotification.getRecords()) {

            // Pegando a notificação de fato
            S3EventNotification.S3Entity s3Entity = s3EventNotificationRecord.getS3();

            // Obtendo o nome do Bucket
            String bucketName = s3Entity.getBucket().getName();

            // Obtendo a chave do objeto, o que identifica unicamente aquele objeto que foi feito o upload
            // Nome do arquivo que foi feito o upload
            String objectKey = s3Entity.getObject().getKey();

            // Baixando o arquivo, e salvando o retorno numa String
            String invoiceFile = downloadObject(bucketName, objectKey);

            // Fazendo o parse da string que tem de fato a nota fiscal
            Invoice invoice = objectMapper.readValue(invoiceFile, Invoice.class);
            LOG.info("Invoice received: {}", invoice.getInvoiceNumber());

            // Salvando o Arquivo no Banco de Dados
            invoiceRepository.save(invoice);

            // Deletando Objeto/Arquivo do Bucket S3
            amazonS3.deleteObject(bucketName, objectKey);
        }
    }

    private String downloadObject(String bucketName, String objectKey) throws IOException {

        // Capturando o objeto apartir do
        // Nome do Bucket, e
        // Chave do Objeto
        S3Object s3Object = amazonS3.getObject(bucketName, objectKey);

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(s3Object.getObjectContent())
        );
        String content = null;
        // Lendo linha a linha com bufferedRead.readLine
        while ((content = bufferedReader.readLine()) != null) {
            stringBuilder.append(content);
        }
        return stringBuilder.toString();
    }
}
