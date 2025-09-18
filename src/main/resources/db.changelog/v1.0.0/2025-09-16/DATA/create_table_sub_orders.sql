CREATE TABLE sub_orders (
   sub_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),     -- GUID ID подзаказа
   order_id UUID NOT NULL UNIQUE,        -- GUID ID заказа
   operation_id VARCHAR(255) ,           -- Идентификатор операции
   external_system_code VARCHAR(50) , -- Код внешней системы
   doc_type VARCHAR(255),                  -- Тип документа
   policy_id VARCHAR(255) NOT NULL,        -- Идентификатор полиса
   policy_number VARCHAR(255) NOT NULL,    -- Номер полиса
   contract_id VARCHAR(255),               -- Идентификатор договора
   contract_number VARCHAR(255) NOT NULL,  -- Номер договора
   insurance_program VARCHAR(255),         -- Программа страхования
   type_insurance VARCHAR(255),            -- Вид страхования
   premium_amount VARCHAR(255),            -- Размер премии
   manager_email VARCHAR(255),             -- Электронная почта менеджера
   create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,      -- Дата создания, автоматически заполняется
   update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Дата обновления, автоматически заполняется
   FOREIGN KEY (external_system_code) REFERENCES client_systems(external_system_code),-- Связь с системой клиента
   FOREIGN KEY (order_id) REFERENCES orders(order_id) -- Связь с айди заказа
);