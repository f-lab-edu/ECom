package com.example.core.domain.product.api;

import com.example.core.domain.product_image.QProductImage;
import com.example.core.dto.ProductSearchConditionDto;
import com.example.core.dto.ProductSearchDto;
import com.example.core.exception.BadRequestException;
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
    public List<ProductSearchDto> findProductsByCondition(ProductSearchConditionDto conditionDto) {
        return queryFactory
                .select(Projections.constructor(
                        ProductSearchDto.class,
                        product.id,
                        product.productName,
                        product.price,
                        product.stockQuantity,
                        product.category.name
                ))
                .from(product)
                .where(buildWhere(conditionDto))
                .orderBy(toOrderSpecifier(conditionDto.getSort()))
                .fetch();
    }

    @Override
    public long countProductsByCondition(ProductSearchConditionDto conditionDto) {
        return Optional.ofNullable(
                queryFactory
                        .select(product.count())
                        .from(product)
                        .where(buildWhere(conditionDto))
                        .fetchOne())
                .orElse(0L);
    }

    private BooleanBuilder buildWhere(ProductSearchConditionDto conditionDto) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(product.isDeleted.eq(false));
        where.and(categoryCondition(conditionDto.getCategoryId()));
        where.and(product.price.between(conditionDto.getMinPrice(), conditionDto.getMaxPrice()));
        return where;
    }

    private OrderSpecifier<?> toOrderSpecifier(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return product.id.desc(); // Default sorting
        }

        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        boolean asc = parts.length < 2 || parts[1].trim().equalsIgnoreCase("asc");

        return switch (field) {
            case "price" -> asc ? product.price.asc() : product.price.desc();
            case "id" -> asc ? product.id.asc() : product.id.desc();
            default -> throw new BadRequestException();
        };
    }

    private BooleanExpression categoryCondition(Long categoryId) {
        if (categoryId == null) return null;
        return product.category.id.eq(categoryId);
    }

}
