# План доработок для реализации API мессенджера

## 📋 Обзор
Согласно `api.md`, требуется реализовать полноценный REST API + WebSocket для Flutter мессенджера с поддержкой аутентификации, чатов, сообщений и файлов.

---

## 1. 🔐 Аутентификация

### Текущее состояние:
- Есть form-based login через Spring Security
- Отсутствует JWT токенизация

### Необходимо реализовать:

#### 1.1 Зависимости
```xml
<!-- В pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

#### 1.2 Модель DTO
- `LoginRequest.java` - запрос авторизации
- `LoginResponse.java` - ответ с токеном и пользователем

#### 1.3 JWT Сервис
- `JwtService.java` - генерация/валидация токенов

#### 1.4 Фильтр JWT Authentication
- `JwtAuthenticationFilter.java` - фильтрация запросов с токеном

#### 1.5 REST Контроллер
```java
POST /api/login
- Принимает: username, password
- Возвращает: token + user (id, name, avatarUrl, status)
```

#### 1.6 Конфигурация Security
- Отключить form login
- Добавить JWT фильтр до UsernamePasswordAuthenticationFilter

---

## 2. 👥 Пользователи

### Текущее состояние:
- Сущность User существует, но не имеет avatarUrl и status полей

### Необходимо реализовать:

#### 2.1 Модификация сущности User
```java
// Добавить поля:
private String name;          // отображаемое имя
private String avatarUrl;     // URL аватара (опционально)
private String status;        // online | offline
```

#### 2.2 Миграция БД
```sql
ALTER TABLE usr ADD COLUMN name VARCHAR(255);
ALTER TABLE usr ADD COLUMN avatar_url VARCHAR(255);
ALTER TABLE usr ADD COLUMN status VARCHAR(20) DEFAULT 'offline';
```

#### 2.3 REST Контроллер
```java
GET /api/users
- Authorization: Bearer <token>
- Возвращает: список всех пользователей с профилями
```

---

## 3. 💬 Чаты

### Текущее состояние:
- Нет сущности Chat

### Необходимо реализовать:

#### 3.1 Сущность Chat
```java
@Entity
@Table(name = "chat")
class Chat {
    private String id;              // UUID
    private String contactId;       // ID собеседника (user_id)
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
```

#### 3.2 Миграция БД
```sql
CREATE TABLE chat (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,      -- владелец чата
    contact_id BIGINT NOT NULL,   -- собеседник
    last_message_text TEXT,
    last_message_time TIMESTAMP,
    unread_count INTEGER DEFAULT 0
);
```

#### 3.3 REST Контроллеры
```java
GET /api/chat/list
- Возвращает: список чатов с последним сообщением

POST /api/chat/personal
- Тело: { "contactId": "user_002" }
- Создает новый личный чат
```

#### 3.4 Chat Repository
```java
interface ChatRepo extends JpaRepository<Chat, String> {
    List<Chat> findByUserIdOrContactId(Long userId, Long contactId);
}
```

---

## 4. 📨 Сообщения

### Текущее состояние:
- Сущность MessageEntity хранит только text и filename
- Нет полей: senderId, receiverId, type, status, fileMeta

### Необходимо реализовать:

#### 4.1 Миграция БД
```sql
-- Удалить старую таблицу message (или создать новую)
CREATE TABLE message_new (
    id VARCHAR(255) PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'text',
    status VARCHAR(20) DEFAULT 'sent',
    timestamp TIMESTAMP,
    file_meta JSONB
);

ALTER TABLE message_new RENAME TO message;
```

#### 4.2 Модификация сущности MessageEntity
```java
@Entity(name = "message")
class MessageEntity {
    private String id;              // UUID
    private Long senderId;
    private Long receiverId;
    private String content;
    private String type;            // text, image, file
    private String status;          // sending, sent, delivered, read
    private LocalDateTime timestamp;
    private FileMeta fileMeta;      // JSONB
}
```

#### 4.3 DTO для сообщений
- `SendMessageRequest.java` - запрос отправки
- `MessageResponse.java` - ответ с созданным сообщением

#### 4.4 REST Контроллеры
```java
GET /api/messages?userId={id}
- Authorization: Bearer <token>
- Возвращает: история сообщений с пользователем

POST /api/messages
- Тело: { receiverId, content, type }
- Возвращает: созданное сообщение
```

---

## 5. 📁 Файлы

### Текущее состояние:
- Есть FileStorageService с базовой загрузкой

### Необходимо реализовать:

#### 5.1 Модификация FileStorageService
```java
// Поддержка загрузки через multipart/form-data
@PostMapping("/api/files/upload")
public ResponseEntity<FileMeta> uploadFile(
    @RequestParam("file") MultipartFile file,
    @RequestParam("type") String type,
    Principal principal)
```

#### 5.2 DTO для файлов
- `FileUploadRequest.java`
- `FileMeta.java` - метаданные файла

#### 5.3 REST Контроллер
```java
POST /api/files/upload
- Content-Type: multipart/form-data
- Поля: file, type (image/file)
- Возвращает: { id, name, size, mimeType, url }
```

---

## 6. 🌐 WebSocket (Real-time)

### Текущее состояние:
- Есть WebSocketConfig с /chat endpoint
- Но используется STOMP, а не прямой WebSocket

### Необходимо реализовать:

#### 6.1 Новый WebSocket Endpoint
```java
@Configuration
@EnableWebSocketMessageBroker
public class RealTimeWebSocketConfig {
    // ws://localhost:8080/ws?token=<token>
}
```

#### 6.2 Структура сообщений (JSON)

**Клиент → Сервер:**
```json
{ "type": "authenticate", "token": "..." }
{ "type": "message.send", "receiverId": "...", "content": "..." }
{ "type": "typing.start", "receiverId": "..." }
{ "type": "typing.stop", "receiverId": "..." }
```

**Сервер → Клиент:**
```json
{ "type": "message.receive", "message": {...} }
{ "type": "status.update", "userId": "...", "status": "online" }
{ "type": "typing.start", "userId": "...", "receiverId": "..." }
```

#### 6.3 WebSocket Handler
```java
@Component
public class RealTimeWebSocketHandler extends TextWebSocketHandler {
    // Обработка всех типов сообщений
}
```

---

## 🔧 Дополнительные задачи

### 7.1 Global Exception Handler
```java
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ErrorResponse> handleUnauthorized(...)
    
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(...)
}
```

### 7.2 Common DTOs
- `ErrorResponse.java` - стандартный формат ошибок

### 7.3 Конфигурация CORS
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // Разрешить запросы от Flutter приложения
}
```

---

## 📁 Структура новых файлов

```
src/main/java/com/test/chat/
├── configuration/
│   ├── JwtAuthenticationFilter.java (новый)
│   └── WebSocketConfig.java (модификация)
├── controller/
│   ├── api/
│   │   ├── AuthController.java (новый)
│   │   ├── UserController.java (новый)
│   │   ├── ChatController.java (новый)
│   │   ├── MessageController.java (новый)
│   │   └── FileController.java (новый)
│   └── RealTimeWebSocketController.java (новый)
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   ├── user/
│   │   ├── UserDto.java
│   │   └── UserProfile.java
│   ├── chat/
│   │   ├── ChatDto.java
│   │   └── CreateChatRequest.java
│   ├── message/
│   │   ├── MessageRequest.java
│   │   ├── MessageResponse.java
│   │   └── MessageHistoryResponse.java
│   └── file/
│       ├── FileUploadRequest.java
│       └── FileMeta.java
├── entity/
│   ├── User.java (модификация)
│   ├── Chat.java (новый)
│   └── MessageEntity.java (модификация)
├── repository/
│   ├── UserRepo.java (модификация)
│   ├── ChatRepo.java (новый)
│   └── MessageRepo.java (модификация)
└── service/
    ├── api/
    │   ├── JwtService.java
    │   ├── UserService.java (модификация)
    │   ├── ChatService.java (новый)
    │   ├── MessageService.java (модификация)
    │   └── FileStorageService.java (модификация)
    └── impl/
        └──JwtServiceImpl.java
```

---

## 🗄️ SQL Миграции

```
src/main/resources/db/migration/
├── V202604270001__add_user_profile_fields.sql (новый)
├── V202604270002__create_chat_table.sql (новый)
├── V202604270003__rename_message_columns.sql (новый)
└── V202604270004__add_file_meta_to_message.sql (новый)
```

---

## 📝 Приоритет реализации

1. **Аутентификация** - основа для всех остальных функций
2. **Пользователи** - необходимы для чатов и сообщений
3. **Сообщения** -核心 функционал мессенджера
4. **Чаты** - организация сообщений в чаты
5. **Файлы** - расширение функциональности
6. **WebSocket Real-time** - улучшение UX

---

## ⚠️ Примечания

- В API используется UUID для id, но текущая БД использует numeric ID
- Требуется решение: мигрировать на UUID или адаптировать API под numeric IDs
- Для поля `status` в User нужно решить, как обновлять статус (heartbeat, WebSocket events)