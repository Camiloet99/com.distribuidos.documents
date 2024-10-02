package com.DocumentManager.document_service.controllers;

import com.DocumentManager.document_service.models.ResponseBody;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@UtilityClass
public class ControllerUtils {
    
    public static <T> ResponseEntity<ResponseBody<T>> ok(T result) {
        ResponseBody<T> orb = ResponseBody
                .<T>builder()
                .status(OK.value())
                .result(result)
                .build();
        
        return ResponseEntity.ok(orb);
    }
    
    public static <T> ResponseEntity<ResponseBody<T>> created(T result) {
        ResponseBody<T> orb = ResponseBody
                .<T>builder()
                .status(CREATED.value())
                .result(result)
                .build();
        
        return ResponseEntity.ok(orb);
    }
    
}
