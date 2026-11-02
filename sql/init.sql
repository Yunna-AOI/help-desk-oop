CREATE ROLE helpdesk_app
    WITH LOGIN PASSWORD 'helpdesk_app';

CREATE DATABASE helpdesk_db
    OWNER helpdesk_app
    ENCODING 'UTF8';

\connect helpdesk_db

SET ROLE helpdesk_app;

CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'SUPPORT_AGENT',
    'ADMIN'
);

CREATE TYPE ticket_status AS ENUM (
    'NEW',
    'IN_PROGRESS',
    'RESOLVED',
    'CLOSED',
    'CANCELLED'
);

CREATE TYPE ticket_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    role          user_role NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    description      TEXT,
    default_priority ticket_priority NOT NULL DEFAULT 'MEDIUM',
    active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sla_policies (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120) NOT NULL UNIQUE,
    priority         ticket_priority NOT NULL UNIQUE,
    response_minutes INTEGER NOT NULL CHECK (response_minutes > 0),
    resolve_minutes  INTEGER NOT NULL CHECK (resolve_minutes > 0)
);

CREATE TABLE tickets (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status      ticket_status NOT NULL DEFAULT 'NEW',
    priority    ticket_priority NOT NULL DEFAULT 'MEDIUM',

    customer_id BIGINT NOT NULL REFERENCES users(id),
    assignee_id BIGINT REFERENCES users(id),
    category_id BIGINT REFERENCES categories(id),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at  TIMESTAMP,
    due_at     TIMESTAMP,
    version    BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT title_not_blank
        CHECK (length(trim(title)) >= 3)
);

CREATE TABLE ticket_comments (
    id         BIGSERIAL PRIMARY KEY,
    ticket_id  BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id  BIGINT NOT NULL REFERENCES users(id),
    text       TEXT NOT NULL,
    internal   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT comment_not_blank
        CHECK (length(trim(text)) > 0)
);

CREATE TABLE ticket_status_history (
    id            BIGSERIAL PRIMARY KEY,
    ticket_id     BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    old_status    ticket_status,
    new_status    ticket_status NOT NULL,
    changed_by_id BIGINT REFERENCES users(id),
    changed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_assignment_history (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    old_assignee_id BIGINT REFERENCES users(id),
    new_assignee_id BIGINT REFERENCES users(id),
    changed_by_id   BIGINT REFERENCES users(id),
    changed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attachments (
    id             BIGSERIAL PRIMARY KEY,
    ticket_id      BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    uploaded_by_id BIGINT NOT NULL REFERENCES users(id),
    original_name  VARCHAR(255) NOT NULL,
    storage_path   VARCHAR(500) NOT NULL,
    content_type   VARCHAR(120),
    size_bytes     BIGINT NOT NULL CHECK (size_bytes >= 0),
    uploaded_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE ticket_tags (
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    tag_id    BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (ticket_id, tag_id)
);

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id),
    action      VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id   BIGINT,
    details     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id);
CREATE INDEX idx_tickets_customer ON tickets(customer_id);
CREATE INDEX idx_tickets_category ON tickets(category_id);
CREATE INDEX idx_tickets_due_at ON tickets(due_at);
CREATE INDEX idx_comments_ticket ON ticket_comments(ticket_id);
CREATE INDEX idx_status_history_ticket ON ticket_status_history(ticket_id);
CREATE INDEX idx_assignment_history_ticket ON ticket_assignment_history(ticket_id);
CREATE INDEX idx_attachments_ticket ON attachments(ticket_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);

CREATE VIEW ticket_details AS
SELECT
    t.id,
    t.title,
    t.status,
    t.priority,
    c.name AS category_name,
    customer.name AS customer_name,
    assignee.name AS assignee_name,
    t.created_at,
    t.updated_at,
    t.due_at
FROM tickets t
JOIN users customer ON customer.id = t.customer_id
LEFT JOIN users assignee ON assignee.id = t.assignee_id
LEFT JOIN categories c ON c.id = t.category_id;

INSERT INTO departments(name, description) VALUES
('Первая линия', 'Приём и первичная обработка обращений'),
('Сетевые инженеры', 'Сеть, Wi-Fi, VPN, маршрутизация'),
('Системные администраторы', 'Серверы и рабочие станции');

INSERT INTO categories(name, description, default_priority) VALUES
('Сеть', 'Wi-Fi, LAN, VPN, доступ в Интернет', 'HIGH'),
('Программное обеспечение', 'Ошибки и установка ПО', 'MEDIUM'),
('Оборудование', 'ПК, принтеры и периферия', 'MEDIUM'),
('Доступы', 'Учётные записи и права', 'HIGH');

INSERT INTO sla_policies(name, priority, response_minutes, resolve_minutes) VALUES
('Критический SLA', 'CRITICAL', 15, 30),
('Высокий SLA', 'HIGH', 30, 120),
('Средний SLA', 'MEDIUM', 60, 480),
('Низкий SLA', 'LOW', 120, 1440);

INSERT INTO users(name, email, role, department_id) VALUES
('Анна Петрова', 'anna@example.org', 'CUSTOMER', NULL),
('Иван Соколов', 'ivan.support@example.org', 'SUPPORT_AGENT', 1),
('Сергей Волков', 'sergey.net@example.org', 'SUPPORT_AGENT', 2),
('Олег Админов', 'admin@example.org', 'ADMIN', 3);

INSERT INTO tags(name) VALUES
('сеть'),
('оборудование'),
('срочно');

INSERT INTO tickets(
    title, description, status, priority, customer_id, category_id, due_at
)
VALUES (
    'Не работает Wi-Fi',
    'Ноутбук подключается к сети, но Интернет недоступен',
    'NEW',
    'HIGH',
    (SELECT id FROM users WHERE email = 'anna@example.org'),
    (SELECT id FROM categories WHERE name = 'Сеть'),
    CURRENT_TIMESTAMP + INTERVAL '2 hours'
);
