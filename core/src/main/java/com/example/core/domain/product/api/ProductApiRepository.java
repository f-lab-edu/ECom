package com.example.core.domain.product.api;

import com.example.core.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductApiRepository extends JpaRepository<Product, Long>, ProductApiRepositoryCustom {

}
