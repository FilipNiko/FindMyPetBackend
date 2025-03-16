package com.spring.findmypet.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import jakarta.annotation.PostConstruct

@Service
class FileStorageService(
    @Value("\${file.upload-dir}")
    private val uploadDir: String,

    @Value("\${file.upload-path}")
    private val baseUploadPath: String
) {
    private lateinit var uploadPath: Path

    @PostConstruct
    fun init() {
        uploadPath = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize()
        
        Files.createDirectories(uploadPath)
    }

    fun storeFile(file: MultipartFile): String {
        val originalFileName = file.originalFilename
        val fileExtension = originalFileName?.substringAfterLast('.', "")
        
        if (fileExtension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException("Nedozvoljena ekstenzija fajla. Dozvoljene ekstenzije su: ${ALLOWED_EXTENSIONS.joinToString()}")
        }

        val fileName = "${UUID.randomUUID()}.$fileExtension"
        val targetLocation = uploadPath.resolve(fileName)
        Files.copy(file.inputStream, targetLocation)

        return "$baseUploadPath/$fileName"
    }

    fun deleteFile(fileName: String) {
        val actualFileName = fileName.substringAfterLast('/')
        val filePath = uploadPath.resolve(actualFileName)
        Files.deleteIfExists(filePath)
    }

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")
    }
} 