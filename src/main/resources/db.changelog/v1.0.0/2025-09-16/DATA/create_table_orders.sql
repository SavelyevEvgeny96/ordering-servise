CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- Создание таблицы заказов
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),                -- GUID ID заказа
    recipient_user_gd_id VARCHAR(255),              -- Идентификатор золотой карточки страхователя
    key_card VARCHAR(255),                          -- Ключ привязки карты (для рекуррентных платежей)
    save_card VARCHAR(255),                         -- Признак необходимости сохранения данных карты
status order_statuses_enum,                         --Статус
    recurrent VARCHAR(255),                         -- Признак рекуррентного платежа
    payment_end_date VARCHAR(255),                  -- Дата окончания действия ссылки
    premium_amount VARCHAR(255),                    -- Размер премии
    recipient_email VARCHAR(255) NOT NULL,          -- Электронная почта страхователя
    recipient_phone VARCHAR(255) NOT NULL,          -- Мобильный телефон страхователя
    recipient_user_id VARCHAR(255),                 -- Идентификатор личного кабинета страхователя
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,   -- Дата создания, автоматически заполняется
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,   -- Дата обновления, автоматически заполняется
    FOREIGN KEY (state_id)REFERENCES order_status(state_id) )          -- Связь со статусом заказа