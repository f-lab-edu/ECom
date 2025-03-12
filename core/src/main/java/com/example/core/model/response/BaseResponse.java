package com.example.core.model.response;

import lombok.*;
import lombok.extern.slf4j.Slf4j;


import java.io.Serializable;
import java.time.LocalDate;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse implements Serializable {

    @Builder.Default
    private String dateTiime = LocalDate.now().toString();

    @Builder.Default
    private ResponseStatus status = new ResponseStatus();

    public static BaseResponse of() {
        BaseResponse successResponse = new BaseResponse();
        return successResponse;
    }

    public static BaseResponse of(ResponseCode responseCode) {
        BaseResponse successResponse = new BaseResponse();
        successResponse.setStatus(new ResponseStatus(responseCode.getCode(), responseCode.getMessage()));
        return successResponse;
    }
}
