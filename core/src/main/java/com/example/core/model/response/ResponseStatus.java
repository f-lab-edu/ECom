package com.example.core.model.response;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseStatus implements Serializable {

    @Builder.Default
    private String code = ResponseCode.SUCCESS.getCode();

    @Builder.Default
    private String message = ResponseCode.SUCCESS.getMessage();
}
