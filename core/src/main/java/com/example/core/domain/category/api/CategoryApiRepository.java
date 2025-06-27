package com.example.core.domain.category.api;

import com.example.core.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryApiRepository extends JpaRepository<Category, Long> {

}
