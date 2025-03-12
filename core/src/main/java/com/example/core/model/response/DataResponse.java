package com.example.core.model.response;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse<T> extends BaseResponse {

    private T data;

    public static <T> DataResponse<T> of(T data) {
        DataResponse dataResponse = new DataResponse();

        dataResponse.setData(data);

        return dataResponse;
    }

    public static <T> DataResponse<T> of(ResponseCode responseCode, T data) {
        DataResponse dataResponse = new DataResponse();

        dataResponse.setStatus(new ResponseStatus(responseCode.getCode(), responseCode.getMessage()));
        dataResponse.setData(data);

        return dataResponse;
    }
}
