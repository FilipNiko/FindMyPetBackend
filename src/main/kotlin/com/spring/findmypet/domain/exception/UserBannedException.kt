package com.spring.findmypet.domain.exception

class UserBannedException(message: String, val reason: String?) : RuntimeException(message)
