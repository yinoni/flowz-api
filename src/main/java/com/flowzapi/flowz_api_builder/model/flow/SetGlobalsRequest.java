package com.flowzapi.flowz_api_builder.model.flow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@Getter
@Setter
public class SetGlobalsRequest {

    @NotNull(message = "Globals map cannot be null")
    Map<String, Object> globals;

    @NotNull(message = "Field name must be a valid enum value")
    private FlowFieldName fieldName; //
}
