package com.DocumentManager.document_service.exceptions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {

    private static final String PREFIX = "DC-00";

    public static final String CENTRALIZER_UPSTREAM_ERROR = PREFIX + "01";

}
