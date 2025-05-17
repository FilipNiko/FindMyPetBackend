# Dokumentacija sistema za poruke - FindMyPet

## Pregled sistema

Sistem za razmenu poruka u FindMyPet aplikaciji omogućava korisnicima da:

1. Šalju poruke drugim korisnicima (posebno vlasnicima ljubimaca)
2. Primaju poruke u realnom vremenu
3. Vide svoje konverzacije (inbox)
4. Vide istoriju poruka za svaku konverzaciju
5. Prate status pročitanih poruka

Sistem je implementiran kombinacijom REST API-ja za standardne operacije i WebSocket-a za komunikaciju u realnom vremenu.

## WebSocket konfiguracija

### Endpoint za povezivanje

Klijenti se povezuju na WebSocket server preko sledećeg endpointa:

```
ws://{server-base-url}/ws
```

### Autentifikacija

**VAŽNO**: WebSocket konekcija zahteva autentifikaciju. Postoje dva načina za slanje tokena:

1. **Putem HTTP headera (preporučeno):**
   ```
   Authorization: Bearer <your-jwt-token>
   ```

2. **Putem URL parametra:**
   ```
   ws://{server-base-url}/ws?token=<your-jwt-token>
   ```

### STOMP Destination Prefiksi

Sistem koristi STOMP (Simple Text Oriented Messaging Protocol) preko WebSocket-a sa sledećim prefiksima:

- `/app` - Prefiks za slanje poruka na server
- `/user` - Prefiks za korisničke destinacije (poruke koje se šalju određenom korisniku)
- `/topic` - Prefiks za teme na koje se pretplatiti (za grupne objave)

## Model podataka

### MessageDto

Predstavlja pojedinačnu poruku u konverzaciji.

```kotlin
data class MessageDto(
    val id: Long? = null,        // ID poruke (null za nove poruke)
    val senderId: Long,          // ID pošiljaoca
    val senderName: String,      // Ime pošiljaoca
    val content: String,         // Sadržaj poruke
    val sentAt: LocalDateTime,   // Vreme slanja
    val isRead: Boolean,         // Status pročitanosti
    val readAt: LocalDateTime?   // Vreme kada je poruka pročitana (null ako nije)
)
```

### MessageRequest

Koristi se za slanje nove poruke.

```kotlin
data class MessageRequest(
    val receiverId: Long,    // ID primaoca
    val content: String      // Sadržaj poruke
)
```

### ConversationDto

Predstavlja konverzaciju između dva korisnika u inbox-u.

```kotlin
data class ConversationDto(
    val id: Long,                   // ID konverzacije
    val otherUserId: Long,          // ID drugog korisnika u konverzaciji
    val otherUserName: String,      // Ime drugog korisnika
    val lastMessage: String?,       // Sadržaj poslednje poruke (null ako nema poruka)
    val lastMessageTime: LocalDateTime?, // Vreme poslednje poruke (null ako nema poruka)
    val unreadCount: Int,           // Broj nepročitanih poruka
    val createdAt: LocalDateTime,   // Vreme kreiranja konverzacije
    val updatedAt: LocalDateTime    // Vreme poslednjeg ažuriranja konverzacije
)
```

### PetMessageRequest

Koristi se za slanje poruka vlasniku ljubimca.

```kotlin
data class PetMessageRequest(
    val content: String  // Sadržaj poruke
)
```

### ApiResponse

Generički omotač oko odgovora API-ja.

```kotlin
data class ApiResponse<T>(
    val success: Boolean,          // Status uspeha zahteva
    val result: T? = null,         // Rezultat (ako je uspešno)
    val errors: List<ApiError> = emptyList() // Lista grešaka (ako ima)
)

data class ApiError(
    val errorCode: String,         // Kod greške
    val errorDescription: String   // Opis greške
)
```

## REST API Endpointi

### 1. Slanje poruke korisniku

**Zahtev:** `POST /api/messages`

**Telo zahteva:**
```json
{
  "receiverId": 123,
  "content": "Zdravo, kako si?"
}
```

**Odgovor:**
```json
{
  "success": true,
  "result": {
    "id": 456,
    "senderId": 789,
    "senderName": "Marko Marković",
    "content": "Zdravo, kako si?",
    "sentAt": "2023-05-15T14:30:45",
    "isRead": false,
    "readAt": null
  }
}
```

### 2. Dobijanje svih konverzacija korisnika (inbox)

**Zahtev:** `GET /api/messages/conversations`

**Odgovor:**
```json
{
  "success": true,
  "result": [
    {
      "id": 1,
      "otherUserId": 123,
      "otherUserName": "Petar Petrović",
      "lastMessage": "Zdravo, kako si?",
      "lastMessageTime": "2023-05-15T14:30:45",
      "unreadCount": 2,
      "createdAt": "2023-05-10T10:15:30",
      "updatedAt": "2023-05-15T14:30:45"
    },
    {
      "id": 2,
      "otherUserId": 456,
      "otherUserName": "Jovana Jovanović",
      "lastMessage": "Vidimo se sutra!",
      "lastMessageTime": "2023-05-14T18:45:20",
      "unreadCount": 0,
      "createdAt": "2023-05-05T09:20:15",
      "updatedAt": "2023-05-14T18:45:20"
    }
  ]
}
```

### 3. Dobijanje poruka iz određene konverzacije

**Zahtev:** `GET /api/messages/conversations/{conversationId}`

**Odgovor:**
```json
{
  "success": true,
  "result": [
    {
      "id": 123,
      "senderId": 456,
      "senderName": "Petar Petrović",
      "content": "Ćao, kako si?",
      "sentAt": "2023-05-14T10:30:45",
      "isRead": true,
      "readAt": "2023-05-14T10:31:20"
    },
    {
      "id": 124,
      "senderId": 789,
      "senderName": "Marko Marković",
      "content": "Dobro sam, hvala! Kako si ti?",
      "sentAt": "2023-05-14T10:32:15",
      "isRead": true, 
      "readAt": "2023-05-14T10:33:05"
    }
  ]
}
```

### 4. Označavanje poruka kao pročitanih

**Zahtev:** `PUT /api/messages/conversations/{conversationId}/read`

**Odgovor:**
```json
{
  "success": true,
  "result": true
}
```

### 5. Slanje poruke vlasniku kućnog ljubimca

**Zahtev:** `POST /api/lost-pets/{petId}/messages`

**Telo zahteva:**
```json
{
  "content": "Zdravo, mislim da sam video vašeg ljubimca!"
}
```

**Odgovor:**
```json
{
  "success": true,
  "result": {
    "id": 456,
    "senderId": 789,
    "senderName": "Marko Marković",
    "content": "Zdravo, mislim da sam video vašeg ljubimca!",
    "sentAt": "2023-05-15T14:30:45",
    "isRead": false,
    "readAt": null
  }
}
```

## WebSocket komunikacija

### 1. Povezivanje na WebSocket

Klijent treba da se poveže na WebSocket endpoint i da uspostavi STOMP sesiju:

```
ws://{server-base-url}/ws
```

Pri povezivanju, klijent mora da pošalje autentifikacioni token kako bi server mogao da identifikuje korisnika.

### 2. Pretplate za primanje poruka

Klijent treba da se pretplati na sledeće destinacije:

#### a. Nove poruke

```
/user/{userId}/queue/messages
```

Format poruke koja se prima:
```json
{
  "messageData": {
    "id": 456,
    "senderId": 789,
    "senderName": "Marko Marković",
    "content": "Zdravo, kako si?",
    "sentAt": "2023-05-15T14:30:45",
    "isRead": false,
    "readAt": null
  },
  "conversationId": 123
}
```

#### b. Status pročitanosti poruka

```
/user/{userId}/queue/read-status
```

Format poruke koja se prima:
```json
{
  "conversationId": 123,
  "messageIds": [456, 457, 458],
  "readByUserId": 789,
  "readByUserName": "Marko Marković",
  "timestamp": 1652627445000
}
```

#### c. Ažuriranje konverzacija (opciono)

```
/topic/conversations/{conversationId}
```

Format poruke koja se prima je isti kao za nove poruke.

#### d. Status pročitanih poruka u konverzaciji (opciono)

```
/topic/conversations/{conversationId}/read-status
```

Format poruke koja se prima je isti kao za status pročitanosti.

### 3. Slanje poruka preko WebSocket-a

Klijenti mogu da šalju poruke direktno preko WebSocket-a na sledeću destinaciju:

```
/app/chat
```

Format poruke koja se šalje:
```json
{
  "receiverId": 123,
  "content": "Zdravo, kako si?"
}
```

### 4. Označavanje poruka kao pročitanih preko WebSocket-a

Klijenti mogu da označe poruke kao pročitane preko WebSocket-a na sledeću destinaciju:

```
/app/read
```

Format poruke koja se šalje:
```
conversationId (kao long vrednost)
```

## Primer implementacije klijenta (Android)

### 1. Povezivanje na WebSocket

```kotlin
val stompClient = StompClient(url = "ws://your-server-url/ws")

stompClient.connect(headers = mapOf("Authorization" to "Bearer $jwtToken"))
```

### 2. Pretplata na poruke

```kotlin
// Pretplata na nove poruke
stompClient.subscribe("/user/$userId/queue/messages") { message ->
  // Obrada nove poruke
  val messageData = message.messageData
  val conversationId = message.conversationId
  // Ažuriranje UI-a
}

// Pretplata na status pročitanosti
stompClient.subscribe("/user/$userId/queue/read-status") { status ->
  // Obrada statusa pročitanosti
  val conversationId = status.conversationId
  val messageIds = status.messageIds
  // Ažuriranje UI-a
}
```

### 3. Slanje poruke

```kotlin
// Preko REST API-ja
api.sendMessage(MessageRequest(receiverId = recipientId, content = messageText))
  .enqueue { response ->
    if (response.isSuccessful) {
      // Poruka uspešno poslata
    } else {
      // Greška pri slanju poruke
    }
  }

// Ili preko WebSocket-a
stompClient.send("/app/chat", MessageRequest(receiverId = recipientId, content = messageText))
```

### 4. Označavanje poruka kao pročitanih

```kotlin
// Preko REST API-ja
api.markMessagesAsRead(conversationId)
  .enqueue { response ->
    if (response.isSuccessful) {
      // Poruke uspešno označene kao pročitane
    } else {
      // Greška pri označavanju poruka
    }
  }

// Ili preko WebSocket-a
stompClient.send("/app/read", conversationId)
```

## Tok komunikacije

### Scenario 1: Slanje poruke korisniku

1. Korisnik A šalje poruku korisniku B
   - `POST /api/messages` sa odgovarajućim telom zahteva
   - ili slanjem poruke na `/app/chat` preko WebSocket-a

2. Server:
   - Čuva poruku u bazi
   - Šalje WebSocket notifikaciju korisniku B na `/user/{userBId}/queue/messages`
   - Šalje WebSocket notifikaciju korisniku A na `/user/{userAId}/queue/messages` (potvrda)

3. Korisnik B dobija notifikaciju o novoj poruci i ažurira UI

### Scenario 2: Označavanje poruka kao pročitanih

1. Korisnik B otvara konverzaciju sa korisnikom A
   - `GET /api/messages/conversations/{conversationId}` da dobije poruke

2. Korisnik B označava poruke kao pročitane
   - `PUT /api/messages/conversations/{conversationId}/read`
   - ili slanjem conversationId na `/app/read` preko WebSocket-a

3. Server:
   - Ažurira status poruka u bazi
   - Šalje WebSocket notifikaciju korisniku A na `/user/{userAId}/queue/read-status`

4. Korisnik A dobija notifikaciju o pročitanim porukama i ažurira UI

## Rukovanje greškama

Ako dođe do greške pri komunikaciji, server će vratiti ApiResponse objekat sa success=false i listom ApiError objekata:

```json
{
  "success": false,
  "result": null,
  "errors": [
    {
      "errorCode": "RESOURCE_NOT_FOUND",
      "errorDescription": "Konverzacija nije pronađena"
    }
  ]
}
```

Klijent treba da proveri status uspeha i prikaže odgovarajuću poruku ako je došlo do greške.

## Sigurnosne napomene

1. WebSocket veza zahteva validni JWT token
2. Korisnici mogu da vide samo svoje konverzacije
3. Poruke se šalju samo direktno povezanim korisnicima
4. Poruke primaocu se šalju samo ako je autentikovan

---

Za sva dodatna pitanja ili nejasnoće, molimo vas da kontaktirate backend tim. 