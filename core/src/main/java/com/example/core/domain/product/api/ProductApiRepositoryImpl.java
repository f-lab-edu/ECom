package com.example.core.domain.product.api;

import com.example.core.domain.product_image.QProductImage;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.core.domain.product.QProduct.product;

@RequiredArgsConstructor
@Repository
public class ProductApiRepositoryImpl implements ProductApiRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSearchDto> search(ProductSearchConditionDto conditionDto) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(isDeleted(false));
        where.and(betweenPrice(conditionDto.getMinPrice(), conditionDto.getMaxPrice()));
        where.and(categoryCondition(conditionDto.getCategoryId()));

        Pageable pageable = PageRequest.of(conditionDto.getPage(), conditionDto.getSize());

        QProductImage thumbnailImage = QProductImage.productImage;

        List<ProductSearchDto> results = queryFactory
                .select(Projections.constructor(
                        ProductSearchDto.class,
                        product.id,
                        product.productName,
                        product.price,
                        thumbnailImage.imageUrl,
                        product.category.name
                ))
                .from(product)
                .leftJoin(product.productImages, thumbnailImage)
                    .on(thumbnailImage.isThumbnail.eq(true))
                .where(where)
                .orderBy(toOrderSpecifier(conditionDto.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        long total = Optional.ofNullable(
                queryFactory
                        .select(product.count())
                        .from(product)
                        .where(where)
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(results, pageable, total);
    }

    private OrderSpecifier<?> toOrderSpecifier(String sortParam) {
        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        boolean asc = parts.length < 2 || parts[1].trim().equalsIgnoreCase("asc");

        if (field.equals("price")) {
            return asc ? product.price.asc() : product.price.desc();
        } else {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }
    }


    private BooleanExpression categoryCondition(Long categoryId) {
        if (categoryId == null || categoryId == -1L) return null;
        return product.category.id.eq(categoryId);
    }

    private BooleanExpression isDeleted(boolean isDeleted) {
        return product.isDeleted.eq(isDeleted);
    }

    private BooleanExpression betweenPrice(int min, int max) {
        return product.price.between(min, max);
    }

}
