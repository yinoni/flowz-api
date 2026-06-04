package com.flowzapi.flowz_api_builder.model.flow;

public enum FlowFieldName {
    GLOBAL_ASSERTIONS,
    GLOBAL_VARIABLES,
    GLOBAL_HEADERS;


    public String toDbName(){
        return switch (this) {
            case GLOBAL_ASSERTIONS -> "globalAssertions";
            case GLOBAL_HEADERS -> "globalHeaders";
            case GLOBAL_VARIABLES -> "globalVariables";
        };
    }
}
