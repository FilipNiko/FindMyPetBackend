package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.service.FileStorageService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.Base64
import org.springframework.web.multipart.MultipartFile
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayInputStream

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<String>> {
        val fileName = fileStorageService.storeFile(file)
        return ResponseEntity.ok(ApiResponse(success = true, result = fileName))
    }

    data class Base64UploadRequest(
        val base64Image: String,
        val fileName: String
    )

    @PostMapping("/upload/base64")
    @PreAuthorize("isAuthenticated()")
    fun uploadBase64(
        @RequestBody request: Base64UploadRequest
    ): ResponseEntity<ApiResponse<String>> {
        // Uklanjamo "data:image/jpeg;base64," ili sličan prefix ako postoji
        val base64Data = request.base64Image.substringAfter("base64,")
        
        // Dekodiramo base64 u bajt niz
        val imageBytes = Base64.getDecoder().decode(base64Data)
        
        // Kreiramo MultipartFile iz bajt niza
        val multipartFile = MockMultipartFile(
            request.fileName,           // ime fajla
            request.fileName,           // originalno ime fajla
            "image/jpeg",              // content type
            ByteArrayInputStream(imageBytes)
        )
        
        val fileName = fileStorageService.storeFile(multipartFile)
        return ResponseEntity.ok(ApiResponse(success = true, result = fileName))
    }
} 