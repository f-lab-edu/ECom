package com.example.core.domain.product_image.api;

import com.example.core.domain.product_image.QProductImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ProductImageApiRepositoryImpl implements ProductImageApiRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QProductImage image = QProductImage.productImage;

    @Override
    public Map<Long, String> findThumbnailsByProductIds(List<Long> productIds) {
        return queryFactory
                .select(image.product.id, image.imageUrl)
                .from(image)
                .where(
                        image.product.id.in(productIds),
                        image.isThumbnail.eq(true)
                )
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(image.product.id),
                        tuple -> tuple.get(image.imageUrl)
                ));
    }
}
