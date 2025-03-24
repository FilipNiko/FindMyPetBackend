# Sistem za rukovanje greškama

Ovaj sistem omogućava modularan i skalabilan pristup rukovanju greškama u aplikaciji, uz održavanje jednostavnosti implementacije.

## Struktura

Sistem je organizovan u nekoliko ključnih komponenti:

1. **ErrorCode interfejs** - Osnovni interfejs za sve kodove grešaka
2. **BaseExceptionHandler** - Apstraktna klasa za obradu grešaka
3. **Moduli grešaka** - Organizovani po domenima:
   - SystemErrorCodes - Sistemske greške
   - ValidationErrorCodes - Validacione greške
   - AuthErrorCodes - Greške autentifikacije
   - *... i druge module koji se mogu dodati po potrebi*

## Kako dodati novi modul za rukovanje greškama

1. **Kreirati enum sa kodovima grešaka**

```kotlin
enum class NoviModulErrorCodes(
    override val code: String,
    override val message: String
) : ErrorCode {
    GRESKA_1("KOD_GRESKE_1", "Poruka greške 1"),
    GRESKA_2("KOD_GRESKE_2", "Poruka greške 2")
}
```

2. **Kreirati ExceptionHandler za taj modul**

```kotlin
@RestControllerAdvice(basePackages = ["com.spring.findmypet.controller.novimodul"])
class NoviModulExceptionHandler : BaseExceptionHandler() {

    @ExceptionHandler(NovaSpecificnaGreska::class)
    fun handleSpecificnuGresku(ex: NovaSpecificnaGreska): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Specifična greška: ${ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, NoviModulErrorCodes.GRESKA_1)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    override fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            val errorCode = mapFieldError(fieldError)
            toApiError(errorCode)
        }
        
        logger.error("Validacione greške: ${errors.map { it.errorCode }}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errors)
    }
    
    private fun mapFieldError(fieldError: FieldError): ErrorCode {
        return when (fieldError.field) {
            "specificnoPolje" -> NoviModulErrorCodes.GRESKA_2
            else -> ValidationErrorCodes.VALIDATION_ERROR
        }
    }
}
```

3. **Kreirati specifične izuzetke za modul (opciono)**

```kotlin
class NovaSpecificnaGreska(val info: String) : RuntimeException("Detalji greske: $info")
```

4. **Ažurirati ErrorMessagesConfig**
   
```kotlin
// U ErrorMessagesConfig.init() metodi
val noviModulErrorCount = NoviModulErrorCodes.entries.size
logger.info("Novi modul kodovi grešaka: {}", noviModulErrorCount)
totalErrorCount += noviModulErrorCount
```

## Prednosti ovog pristupa

1. **Modularnost** - Svaki domen ima svoje kodove i handlere
2. **Jednostavnost** - Direktno mapiranje grešaka bez kompleksne hijerarhije
3. **Skalabilnost** - Lako dodavanje novih kodova i handlera
4. **Čitak kod** - Jasno definisano ponašanje za svaki tip greške

## Kako baciti grešku

```kotlin
// Baciti sistemsku grešku
throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_ERROR")

// Baciti autentifikacionu grešku
throw BadCredentialsException("Pogrešne kredencijale")

// Baciti specifičnu grešku
throw IllegalStateException("EMAIL_ALREADY_EXISTS")
```

## Kako testirati rukovanje greškom

Primer testa:

```kotlin
@Test
fun testVracaIspravnuGresku() {
    mockMvc.perform(
        post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email": "", "password": ""}""")
    )
    .andExpect(status().isBadRequest)
    .andExpect(jsonPath("$.success").value(false))
    .andExpect(jsonPath("$.errors[0].errorCode").value("EMAIL_FIELD_REQUIRED"))
}
``` 