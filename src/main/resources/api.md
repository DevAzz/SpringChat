# Полное Описание API для Flutter Мессенджера

## 📋 Содержание
1. [Аутентификация](#аутентификация)
2. [Пользователи](#пользователи)
3. [Чаты](#чаты)
4. [Сообщения](#сообщения)
5. [Файлы](#файлы)
6. [WebSocket (Real-time)](#websocket-real-time)

---

## 🔐 Аутентификация

### POST `/api/login`
**Описание:** Авторизация пользователя по логину и паролю.

**Заголовки:**
- `Content-Type: application/json`

**Тело запроса:**
```json
{
  "username": "user123",
  "password": "secure_password"
}
```

**Успешный ответ (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user_001",
    "name": "Иван Иванов",
    "avatarUrl": "https://example.com/avatar.jpg",
    "status": "online"
  }
}
```

**Ошибки:**
- `401 Unauthorized` - неверные учетные данные
- `400 Bad Request` - некорректный формат данных

---

## 👥 Пользователи

### GET `/api/users`
**Описание:** Получение списка всех доступных пользователей (контактов).

**Заголовки:**
- `Authorization: Bearer <token>`

**Успешный ответ (200 OK):**
```json
[
  {
    "id": "user_001",
    "name": "Иван Иванов",
    "avatarUrl": "https://example.com/avatar.jpg",
    "status": "online"
  },
  {
    "id": "user_002", 
    "name": "Мария Петрова",
    "avatarUrl": null,
    "status": "offline"
  }
]
```

---

## 💬 Чаты

### GET `/api/chat/list`
**Описание:** Получение списка чатов текущего пользователя.

**Заголовки:**
- `Authorization: Bearer <token>`

**Успешный ответ (200 OK):**
```json
[
  {
    "id": "chat_001",
    "contactId": "user_002",
    "lastMessageText": "Привет!",
    "lastMessageTime": "2026-04-27T18:30:00Z",
    "unreadCount": 2,
    "contact": {
      "id": "user_002",
      "name": "Мария Петрова",
      "avatarUrl": null,
      "status": "offline"
    }
  }
]
```

**Примечание:** Поле `contact` может быть опциональным (для экономии трафика).

---

### POST `/api/chat/personal`
**Описание:** Создание личного чата с контактом.

**Заголовки:**
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Тело запроса:**
```json
{
  "contactId": "user_002"
}
```

**Успешный ответ (201 Created):**
```json
{
  "id": "chat_003",
  "contactId": "user_002",
  "lastMessageText": null,
  "lastMessageTime": null,
  "unreadCount": 0,
  "contact": {
    "id": "user_002",
    "name": "Мария Петрова"
  }
}
```

---

## 📨 Сообщения

### GET `/api/messages`
**Описание:** Получение истории сообщений с конкретным пользователем.

**Заголовки:**
- `Authorization: Bearer <token>`

**Параметры запроса:**
- `userId` (обязательный) - ID собеседника

**Успешный ответ (200 OK):**
```json
[
  {
    "id": "msg_001",
    "senderId": "user_001",
    "receiverId": "user_002",
    "content": "Привет!",
    "type": "text",
    "status": "read",
    "timestamp": "2026-04-27T18:30:00Z",
    "fileMeta": null
  },
  {
    "id": "msg_002",
    "senderId": "user_002",
    "receiverId": "user_001", 
    "content": "Привет! Как дела?",
    "type": "text",
    "status": "read",
    "timestamp": "2026-04-27T18:31:00Z",
    "fileMeta": null
  }
]
```

**Поля ответа:**
- `senderId` - ID отправителя
- `receiverId` - ID получателя  
- `content` - текст сообщения или URL файла
- `type` - тип сообщения (`text`, `image`, `file`)
- `status` - статус доставки (`sending`, `sent`, `delivered`, `read`)
- `timestamp` - время отправки (ISO 8601)
- `fileMeta` - метаданные файла (опционально)

---

### POST `/api/messages`
**Описание:** Отправка сообщения.

**Заголовки:**
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Тело запроса:**
```json
{
  "receiverId": "user_002",
  "content": "Привет! Как дела?",
  "type": "text"
}
```

**Успешный ответ (201 Created):**
```json
{
  "id": "msg_003",
  "senderId": "user_001",
  "receiverId": "user_002",
  "content": "Привет! Как дела?",
  "type": "text",
  "status": "sent",
  "timestamp": "2026-04-27T18:35:00Z",
  "fileMeta": null
}
```

---

## 📁 Файлы

### POST `/api/files/upload`
**Описание:** Загрузка файла на сервер.

**Заголовки:**
- `Authorization: Bearer <token>`
- Content-Type: `multipart/form-data`

**Тело запроса (multipart/form-data):**
| Поле | Тип | Описание |
|------|-----|----------|
| file | File | Загружаемый файл |
| type | String | Тип файла (`image`, `file`) |

**Успешный ответ (201 Created):**
```json
{
  "id": "file_001",
  "name": "photo.jpg",
  "size": 1048576,
  "mimeType": "image/jpeg",
  "url": "https://example.com/uploads/file_001.jpg"
}
```

**Поля ответа:**
- `id` - уникальный идентификатор файла
- `name` - имя файла
- `size` - размер в байтах
- `mimeType` - MIME тип
- `url` - URL для скачивания (опционально)

---

## 🌐 WebSocket (Real-time)

### Подключение
```
ws://localhost:8080/ws?token=<your_token>
```

### Сообщения от клиента к серверу:

**1. Аутентификация**
```json
{
  "type": "authenticate",
  "token": "eyJhbG..."
}
```

**2. Отправка сообщения**
```json
{
  "type": "message.send",
  "receiverId": "user_002",
  "content": "Привет!",
  "fileMeta": null
}
```

**3. Статус набора текста (начало)**
```json
{
  "type": "typing.start",
  "receiverId": "user_002"
}
```

**4. Статус набора текста (конец)**
```json
{
  "type": "typing.stop",
  "receiverId": "user_002"
}
```

### Сообщения от сервера к клиенту:

**1. Получение сообщения**
```json
{
  "type": "message.receive",
  "message": {
    "id": "msg_004",
    "senderId": "user_002",
    "receiverId": "user_001",
    "content": "Привет!",
    "type": "text",
    "status": "sent",
    "timestamp": "2026-04-27T18:40:00Z"
  }
}
```

**2. Обновление статуса пользователя**
```json
{
  "type": "status.update",
  "userId": "user_002",
  "status": "online"
}
```

**3. Пользователь печатает...**
```json
{
  "type": "typing.start",
  "userId": "user_002",
  "receiverId": "user_001"
}
```

---

## 📝 Описание ошибок

| Код | Описание |
|-----|----------|
| `400` | Некорректный запрос |
| `401` | Не авторизован (неверный/отсутствующий токен) |
| `403` | Запрещено (нет прав доступа) |
| `404` | Ресурс не найден |
| `500` | Внутренняя ошибка сервера |

---

## 🔐 Аутентификация через HTTP заголовки

Все защищенные эндпоинты требуют Bearer token:
```
Authorization: Bearer <your_jwt_token>
```

---

## 📦 Типы данных (Models)

### User
```json
{
  "id": "string",
  "name": "string",
  "avatarUrl": "string | null",
  "status": "online | offline"
}
```

### Message
```json
{
  "id": "string",
  "senderId": "string",
  "receiverId": "string", 
  "content": "string",
  "type": "text | image | file",
  "status": "sending | sent | delivered | read",
  "timestamp": "ISO8601 string",
  "fileMeta": "FileMeta | null"
}
```

### Chat
```json
{
  "id": "string",
  "contactId": "string",
  "lastMessageText": "string | null",
  "lastMessageTime": "ISO8601 string | null",
  "unreadCount": "integer",
  "contact": "User | null"
}
```

### FileMeta
```json
{
  "id": "string",
  "name": "string",
  "size": "integer",
  "mimeType": "string",
  "url": "string | null"
}