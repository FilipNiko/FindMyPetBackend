package com.spring.findmypet.domain.exception

class EmailAlreadyExistsException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)
class InvalidTokenException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message) 