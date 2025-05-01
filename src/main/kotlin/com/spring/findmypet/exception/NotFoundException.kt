package com.spring.findmypet.exception

/**
 * Izuzetak koji se baca kada traženi resurs nije pronađen
 */
class NotFoundException(message: String) : RuntimeException(message) 