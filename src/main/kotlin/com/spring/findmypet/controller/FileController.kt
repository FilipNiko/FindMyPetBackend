package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.service.FileStorageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<String>> {
        val fileUrl = fileStorageService.storeFile(file)
        return ResponseEntity.ok(ApiResponse(success = true, result = fileUrl))
    }

    @PostMapping("/upload/multiple", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun uploadMultipleFiles(@RequestParam("files") files: List<MultipartFile>): ResponseEntity<ApiResponse<List<String>>> {
        if (files.size > 5) {
            throw IllegalArgumentException("Maksimalan broj fajlova je 5")
        }
        
        val fileUrls = files.map { fileStorageService.storeFile(it) }
        return ResponseEntity.ok(ApiResponse(success = true, result = fileUrls))
    }
} 