package com.suse.manager.api.docs;

import com.suse.manager.api.ApiResponseWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiResponse(
    responseCode = "200",
    description = "1 on success, exception thrown otherwise.",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = IntegerResponse.class)
    )
)
public @interface APIIntegerResponse { }

interface IntegerResponse extends ApiResponseWrapper<Integer> {}
