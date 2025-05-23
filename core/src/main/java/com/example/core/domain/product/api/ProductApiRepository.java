package com.example.core.domain.product.api;

import com.example.core.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductApiRepository extends JpaRepository<Product, Long>, ProductApiRepositoryCustom {

    Optional<Product> findByIdAndIsDeletedFalse(Long id);
}
