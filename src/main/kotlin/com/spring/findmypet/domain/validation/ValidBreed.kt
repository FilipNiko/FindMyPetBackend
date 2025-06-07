package com.spring.findmypet.domain.validation

import com.spring.findmypet.domain.model.PetType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

interface PetWithBreed {
    val petType: PetType
    val breed: String?
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [BreedValidator::class])
annotation class ValidBreed(
    val message: String = "Rasa je obavezna za pse i mačke",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = []
)

class BreedValidator : ConstraintValidator<ValidBreed, PetWithBreed> {
    override fun isValid(request: PetWithBreed?, context: ConstraintValidatorContext): Boolean {
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