package com.example.core.model.response;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> extends BaseResponse {

    private List<T> list;

    private int page; // 0-based

    private int totalPages; // 전체 페이지 수

    private int pageSize; // 페이지 크기

    private int totalCount;

    public static <T> PageResponse<T> of(Page<T> data) {
        PageResponse<T> pageResponse = new PageResponse<T>();

        Pageable pageable = data.getPageable();

        pageResponse.setList(data.getContent());
        pageResponse.setPage(pageable.getPageNumber() + 1); // 0-based to 1-based
        pageResponse.setPageSize(pageable.getPageSize());
        pageResponse.setTotalPages(data.getTotalPages());
        pageResponse.setTotalCount((int) data.getTotalElements());

        return pageResponse;
    }
}
