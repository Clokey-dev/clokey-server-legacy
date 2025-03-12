package com.clokey.server.domain.report.exception;

import com.clokey.server.global.error.code.BaseErrorCode;
import com.clokey.server.global.error.exception.GeneralException;

public class ReportException extends GeneralException {

    public ReportException(BaseErrorCode code) {
        super(code);
    }
}
