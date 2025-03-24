package com.spring.findmypet.domain.validation

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor

@Configuration
class ValidationConfig {

    @Bean
    fun validator(): LocalValidatorFactoryBean {
        val validatorFactoryBean = LocalValidatorFactoryBean()
        validatorFactoryBean.setValidationMessageSource(validationMessageSource())
        return validatorFactoryBean
    }

    @Bean
    fun validationMessageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("validation/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    fun methodValidationPostProcessor(): MethodValidationPostProcessor {
        val methodValidationPostProcessor = MethodValidationPostProcessor()
        methodValidationPostProcessor.setValidator(validator())
        return methodValidationPostProcessor
    }
} 