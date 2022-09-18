package br.com.dev.thiagomds.catalogo_produtos.model;

import javax.persistence.*;

// Constraint no Banco de Dados
// Definindo que o "invoiceNumber" é único
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"invoiceNumber"})
        }
)
@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Referenciação única no banco de dados

    @Column(length = 32, nullable = false)
    private String invoiceNumber; // Número da Nota Fiscal

    @Column(length = 32, nullable = false)
    private String customerName; // Nome do Cliente

    private float totalValue; // Valor Total

    private long productId; // ID do Produto

    private int quantity; // Quantidade

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public float getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(float totalValue) {
        this.totalValue = totalValue;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
