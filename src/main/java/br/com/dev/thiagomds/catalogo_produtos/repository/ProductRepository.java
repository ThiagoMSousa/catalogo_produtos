package br.com.dev.thiagomds.catalogo_produtos.repository;

import br.com.dev.thiagomds.catalogo_produtos.model.Product;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
@ComponentScan
public interface ProductRepository extends CrudRepository<Product, Long> {

    Optional<Product> findByCode(String code);


}
