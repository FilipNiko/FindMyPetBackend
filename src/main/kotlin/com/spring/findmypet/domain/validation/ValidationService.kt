package com.spring.findmypet.domain.validation

import com.spring.findmypet.domain.dto.ApiError
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.stereotype.Service

@Service
class ValidationService {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    fun validate(obj: Any): List<ApiError> {
        val violations = validator.validate(obj)

        val groupedByField = violations.groupBy { it.propertyPath.toString() }

        return groupedByField.flatMap { (field, fieldViolations) ->
            val requiredViolations = fieldViolations.filter { isRequiredViolation(it) }

            if (requiredViolations.isNotEmpty()) {
                requiredViolations.map { violation ->
                    val errorCode = field.uppercase() + "_ERROR"
                    ApiError(
                        errorCode = errorCode,
                        errorDescription = violation.message
                    )
                }
            } else {
                fieldViolations.map { violation ->
                    val errorCode = field.uppercase() + "_ERROR"
                    ApiError(
                        errorCode = errorCode,
                        errorDescription = violation.message
                    )
                }
            }
        }
    }

    private fun isRequiredViolation(violation: ConstraintViolation<*>): Boolean {
        val annotationTypes = violation.constraintDescriptor.annotation.annotationClass.java.name
        return annotationTypes == NotNull::class.java.name ||
               annotationTypes == NotBlank::class.java.name ||
               annotationTypes == NotEmpty::class.java.name
    }
} 