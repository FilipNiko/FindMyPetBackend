package com.spring.findmypet.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val result: T? = null,
    val errors: List<ApiError>? = null
)

data class ApiError(
    val errorCode: String,
    val errorDescription: String
) 