CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- Создание таблицы заказов
CREATE TABLE orders (
    order_id UUID PRIMARY KEY,                -- GUID ID заказа
    recipient_user_gd_id VARCHAR(255),              -- Идентификатор золотой карточки страхователя
    key_card VARCHAR(255),                          -- Ключ привязки карты (для рекуррентных платежей)
    save_card BOOLEAN,                         -- Признак необходимости сохранения данных карты
    status order_statuses_enum,                         --Статус
    recurrent BOOLEAN,                         -- Признак рекуррентного платежа
    payment_end_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- Дата окончания действия ссылки
    premium_amount NUMERIC(19,2),                    -- Размер премии
    recipient_email VARCHAR(255) NOT NULL,          -- Электронная почта страхователя
    recipient_phone VARCHAR(255) NOT NULL,          -- Мобильный телефон страхователя
    recipient_user_id VARCHAR(255),                 -- Идентификатор личного кабинета страхователя
   create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(), -- Дата создания, автоматически заполняется
   update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now())  -- Дата обновления, автоматически заполняется
