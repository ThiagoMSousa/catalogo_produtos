package br.com.dev.thiagomds.catalogo_produtos.repository;

import br.com.dev.thiagomds.catalogo_produtos.model.Invoice;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerName(String customerName);

}
