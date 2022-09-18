package br.com.dev.thiagomds.catalogo_produtos.controller;

import br.com.dev.thiagomds.catalogo_produtos.model.Invoice;
import br.com.dev.thiagomds.catalogo_produtos.model.UrlResponse;
import br.com.dev.thiagomds.catalogo_produtos.repository.InvoiceRepository;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Value("${aws.s3.bucket.invoice.name}")
    private String bucketName;

    private AmazonS3 amazonS3; // Representa o cliente do AmazonS3
    private InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceController(AmazonS3 amazonS3, InvoiceRepository invoiceRepository) {
        this.amazonS3 = amazonS3;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createInvoiceUrl() {
        UrlResponse urlResponse = new UrlResponse();
        // Tempo de Expiração
        // Data e Hora de Execução do Método + 5 minutos
        // Ou seja, a URL tem validade de 5 minutos após a requisição
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));

        // Geração Aleatório do ID/Identificador do Processo
        String processId = UUID.randomUUID().toString();

        // Gerando a URL Pré-Assinada
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                // Criando Modelo
                // Passando o BucketName e Chave/Id do Processo
                // Do Objeto à ser Inserido, para fins de Identificação
                new GeneratePresignedUrlRequest(bucketName, processId)
                        // URL Pré-Assinada será acessada pelo método PUT
                        .withMethod(HttpMethod.PUT)
                        // Passando a Data de Expiração
                        .withExpiration(Date.from(expirationTime));

        // Data/Tempo de Expiração em segundos
        urlResponse.setExpirationTime(expirationTime.getEpochSecond());
        // Atríbuindo a URL Pré-Assinada
        urlResponse.setUrl(
                amazonS3.generatePresignedUrl(generatePresignedUrlRequest)
                    .toString()
        );


        return new ResponseEntity<UrlResponse>(urlResponse, HttpStatus.OK);

    }

    @GetMapping
    // Buscando todas as notas fiscais
    public Iterable<Invoice> findAll() { return invoiceRepository.findAll(); }

    @GetMapping(path = "/bycustomername")
    // Buscando as notas ficais pelo customerName
    public Iterable<Invoice> findByCustomerName(@RequestParam String customerName) {
        return invoiceRepository.findByCustomerName(customerName);
    }


}
