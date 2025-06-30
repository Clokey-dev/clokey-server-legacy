package com.clokey.server.global.config;


import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class H2MySQLCompatibleDialect extends H2Dialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        // DATE_FORMAT 함수 등록
        functionContributions.getFunctionRegistry().register(
                "date_format",
                new StandardSQLFunction("formatdatetime", StandardBasicTypes.STRING)
        );
    }

}


