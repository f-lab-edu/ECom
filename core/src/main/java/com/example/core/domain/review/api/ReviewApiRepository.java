package com.example.core.domain.review.api;

import com.example.core.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewApiRepository extends JpaRepository<Review, Long> {

}
