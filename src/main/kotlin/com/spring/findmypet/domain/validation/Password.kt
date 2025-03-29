package com.spring.findmypet.domain.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class Password(
    val message: String = "Invalid password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = []
)

class PasswordValidator : ConstraintValidator<Password, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Lozinka je obavezna")
                .addConstraintViolation()
            return false
        }

        context.disableDefaultConstraintViolation()
        
        when {
            value.length < 6 -> {
                context.buildConstraintViolationWithTemplate("Lozinka mora imati najmanje 6 karaktera")
                    .addConstraintViolation()
                return false
            }
            !value.contains(Regex("[A-Z]")) -> {
                context.buildConstraintViolationWithTemplate("Lozinka mora sadržati bar jedno veliko slovo")
                    .addConstraintViolation()
                return false
            }
            !value.contains(Regex("[0-9]")) -> {
                context.buildConstraintViolationWithTemplate("Lozinka mora sadržati bar jedan broj")
                    .addConstraintViolation()
                return false
            }
            !value.contains(Regex("[@\$!%#*?&]")) -> {
                context.buildConstraintViolationWithTemplate("Lozinka mora sadržati bar jedan specijalni karakter")
                    .addConstraintViolation()
                return false
            }
        }
        
        return true
    }
} 