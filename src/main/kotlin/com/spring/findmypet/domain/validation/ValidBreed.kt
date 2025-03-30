package com.spring.findmypet.domain.validation

import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.domain.model.PetType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [BreedValidator::class])
annotation class ValidBreed(
    val message: String = "Rasa je obavezna za pse i mačke",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = []
)

class BreedValidator : ConstraintValidator<ValidBreed, ReportLostPetRequest> {
    override fun isValid(request: ReportLostPetRequest?, context: ConstraintValidatorContext): Boolean {
        if (request == null) return true

        if (request.petType != PetType.DOG && request.petType != PetType.CAT) {
            return true
        }
        if (request.breed.isNullOrBlank()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Rasa je obavezna za pse i mačke")
                .addPropertyNode("breed")
                .addConstraintViolation()
            return false
        }
        return true
    }
} 