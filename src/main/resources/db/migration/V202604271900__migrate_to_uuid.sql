-- Миграция на UUID для всех сущностей

-- Установка расширения для генерации UUID
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Удаление старых последовательностей
DROP SEQUENCE IF EXISTS usr_id_seq CASCADE;
DROP SEQUENCE IF EXISTS message_id_seq CASCADE;

-- Изменение типа колонки id в usr с bigint на uuid
ALTER TABLE usr DROP COLUMN id CASCADE;
ALTER TABLE usr ADD COLUMN id uuid;

-- Добавление constraint по умолчанию для генерации UUID v7
ALTER TABLE usr ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Изменение типа колонки id в message с integer на uuid
ALTER TABLE message DROP COLUMN id CASCADE;
ALTER TABLE message ADD COLUMN id uuid;

-- Добавление constraint по умолчанию для генерации UUID v7
ALTER TABLE message ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Удаление старых ограничений (если есть)
ALTER TABLE user_role DROP CONSTRAINT IF EXISTS fk_user_user_id;

-- Пересоздание foreign key с новым типом данных
-- Сначала удаляем старую колонку и создаем новую, так как PostgreSQL не умеет менять тип foreign key напрямую

-- Добавляем временные колонки для переноса данных
ALTER TABLE user_role ADD COLUMN user_id_new uuid;

-- Копируем данные с преобразованием
UPDATE user_role SET user_id_new = (SELECT id FROM usr WHERE usr.id::text = user_role.user_id::text);

-- Удаляем старую колонку и переименовываем новую
ALTER TABLE user_role DROP COLUMN user_id;
ALTER TABLE user_role RENAME COLUMN user_id_new TO user_id;

-- Изменяем тип колонки в usr для foreign key (должен совпадать)
ALTER TABLE usr ALTER COLUMN id TYPE uuid USING id::uuid;

-- Добавляем primary key если его нет
ALTER TABLE usr ADD CONSTRAINT usr_pkey PRIMARY KEY (id);
ALTER TABLE message ADD CONSTRAINT message_pkey PRIMARY KEY (id);

-- Добавляем foreign key constraint для user_role
ALTER TABLE user_role ADD CONSTRAINT fk_user_user_id FOREIGN KEY (user_id) REFERENCES usr(id);