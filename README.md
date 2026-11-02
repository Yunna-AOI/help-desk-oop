# Help Desk OOP — практическая работа №2 (PostgreSQL + JDBC)

## Итог
Приложение Help Desk переведено с хранения в памяти на PostgreSQL. Java 17+ работает с базой через JDBC (`PreparedStatement`, `ResultSet`, транзакции). В коде нет комментариев: все пояснения собраны здесь.

## Быстрый запуск

Нужен JDK 17+ (именно JDK, а не JRE) и Maven — установленный в системе или встроенный в IntelliJ IDEA.

```bash
docker compose up -d
export DB_PASSWORD=helpdesk_app
mvn compile
mvn exec:java
```

Проверка контейнера и БД:

```bash
docker compose ps
PGPASSWORD=helpdesk_app psql -h localhost -p 5439 -U helpdesk_app -d helpdesk_db
```

Переменные окружения:

| Переменная | Значение по умолчанию |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5439/helpdesk_db` |
| `DB_USER` | `helpdesk_app` |
| `DB_PASSWORD` | обязательна, в коде не хранится |

Полный сброс базы (удаляет данные):

```bash
docker compose down -v
docker compose up -d
```

`sql/init.sql` выполняется автоматически только при создании нового volume. Он создаёт роль `helpdesk_app`, базу `helpdesk_db`, типы, таблицы, индексы, VIEW и тестовые данные.

## Почему Docker на 5439
Локальный PostgreSQL занимает стандартный порт `5432`, поэтому учебная база поднята отдельным контейнером на `5439`. Это не мешает существующему серверу и позволяет сбросить учебные данные независимо от него.

## Структура

```
docker-compose.yml          PostgreSQL 16-alpine, порт 5439, volume, init.sql
sql/init.sql                DDL, индексы, VIEW, тестовые данные
src/main/java/
  config/DatabaseConfig.java        подключение через env
  model/                            сущности и enum'ы
  repository/                       интерфейсы репозиториев
  repository/jdbc/                  JDBC-реализации
  service/TicketService.java        бизнес-логика
  Main.java                         обязательный демонстрационный сценарий
```

## Модель данных

| Таблица | Назначение | Связи |
|---|---|---|
| `departments` | подразделения поддержки | `1:N users` |
| `users` | клиенты, агенты, администраторы | `department_id -> departments` |
| `categories` | категории обращений | `1:N tickets` |
| `sla_policies` | сроки реакции/решения по приоритету | `1:1` по `priority` |
| `tickets` | заявки | `customer_id`, `assignee_id`, `category_id` |
| `ticket_comments` | комментарии и внутренние заметки | `ticket_id`, `author_id` |
| `ticket_status_history` | история переходов статуса | `ticket_id`, `changed_by_id` |
| `ticket_assignment_history` | история назначений | `ticket_id`, старый/новый исполнитель |
| `attachments` | метаданные вложений | `ticket_id`, `uploaded_by_id` |
| `tags`, `ticket_tags` | теги many-to-many | `ticket_id`, `tag_id` |
| `audit_log` | аудит действий | `user_id`, `entity_type`, `entity_id` |

Наследование `User -> Customer / SupportAgent / Administrator` в БД не разложено на три таблицы: тип хранится в `users.role`. Это осознанный выбор для учебной модели. Объектная модель и реляционная схема не обязаны совпадать один к одному; важно, чтобы маппинг был явным и предсказуемым.

`assignee_id` допускает `NULL`: у новой заявки исполнителя ещё нет. Поэтому JOIN с исполнителем — `LEFT JOIN`, иначе неназначенные заявки исчезли бы из отчёта.

## Слои и ответственность

- `model` — данные и простые правила (`TicketStatus.canTransitionTo`), без SQL.
- `repository` — контракт доступа к данным.
- `repository/jdbc` — SQL, `PreparedStatement`, маппинг `ResultSet -> объект`.
- `service` — бизнес-правила: проверка клиента, активности категории, расчёт дедлайна по SLA, уведомления.
- `Main` — только сценарий запуска и печать результатов.

`TicketService` зависит от интерфейсов репозиториев, а не от JDBC-классов. `JdbcTicketRepository` можно заменить на `InMemoryTicketRepository`, не меняя сервис.

## Транзакция смены статуса

`JdbcTicketRepository.changeStatus`:

1. `SELECT status ... FOR UPDATE` — блокирует строку заявки от конкурентного изменения.
2. Проверяет допустимость перехода через `TicketStatus.canTransitionTo`.
3. Обновляет `tickets.status`, `updated_at`, при `CLOSED` выставляет `closed_at`, увеличивает `version`.
4. Вставляет строку в `ticket_status_history`.
5. Пишет запись в `audit_log`.
6. Выполняет `COMMIT`; при любом исключении — `ROLLBACK`.

Допустимые переходы:

```
NEW         -> IN_PROGRESS | CANCELLED
IN_PROGRESS -> RESOLVED    | CANCELLED
RESOLVED    -> CLOSED      | IN_PROGRESS
CLOSED      -> —
CANCELLED   -> —
```

`SELECT ... FOR UPDATE` нужен, чтобы два пользователя не прочитали один и тот же старый статус и не записали противоречивую историю. Дополнительно `tickets.version` реализует optimistic locking в обычном `update`.

## Назначение заявки

`assign` выполняется одной транзакцией:

1. Проверяет, что пользователь существует, активен и имеет роль `SUPPORT_AGENT`.
2. Блокирует заявку.
3. Обновляет `assignee_id`; если заявка была `NEW`, переводит её в `IN_PROGRESS`.
4. Пишет `ticket_assignment_history`.
5. При `NEW -> IN_PROGRESS` пишет `ticket_status_history`.
6. Пишет `audit_log` и делает `COMMIT/ROLLBACK`.

## Комментарии

`CommentRepository` умеет отдавать все комментарии и только внешние (`internal = false`). Добавление комментария и запись в `audit_log` выполняются в одной транзакции.

## JOIN и VIEW

`VIEW ticket_details` сразу отдаёт заявку с именем клиента, именем исполнителя и категорией. `findDetails()`/`findDetailsById()` читают это представление через JDBC. Эквивалентный JOIN используется в отчётах и в запросах истории.

## Отчёты

`JdbcReportRepository`:

- количество заявок по статусам (`GROUP BY status`);
- количество по категориям (`LEFT JOIN` + `GROUP BY`);
- нагрузка агентов (`COUNT(...) FILTER (WHERE ...)`);
- среднее время решения (`AVG(EXTRACT(EPOCH FROM (closed_at - created_at)))`).

## Обязательный сценарий (`Main`)

1. Проверка подключения и вывод версии PostgreSQL.
2. Поиск клиента по email.
3. Вывод категорий.
4. Создание заявки от клиента.
5. Получение реального `id` из PostgreSQL через `RETURNING`.
6. Повторное чтение через `findById`.
7. Назначение `SUPPORT_AGENT` (заявка переходит в `IN_PROGRESS`).
8. Клиентский комментарий.
9. `IN_PROGRESS -> RESOLVED`.
10. Внутренний комментарий агента.
11. `RESOLVED -> CLOSED`.
12. Вывод истории статусов.
13. Вывод истории назначений и JOIN-карточки.
14. Вывод всех заявок из БД — после перезапуска данные остаются.

Дополнительно демонстрируются: теги, вложение, отчёты, просроченные заявки, пагинация, `ILIKE`-поиск и audit log.

## Что реализовано из самостоятельной части

Уровень 1:

- поиск пользователя по email (`findByEmail`);
- фильтр заявок по статусу и приоритету одновременно;
- `updated_at` обновляется при каждом изменении;
- отмена заявки (`NEW/IN_PROGRESS -> CANCELLED`);
- деактивация категории и запрет создания заявки в неактивной категории;
- вывод только внешних комментариев;
- запрет назначения пользователя без роли `SUPPORT_AGENT`;
- карточка заявки с номером, темой, статусом, приоритетом, клиентом, исполнителем, категорией и датами.

Уровень 2:

- таблица `sla_policies` и расчёт `tickets.due_at`;
- просроченные незакрытые заявки;
- пагинация `LIMIT/OFFSET`;
- поиск по `title`/`description` без учёта регистра (`ILIKE`);
- история назначений;
- метаданные вложений;
- статистика по каждому агенту одним `GROUP BY`;
- среднее время решения по категориям.

Уровень 3:

- теги many-to-many (`tags`, `ticket_tags`);
- optimistic locking через `tickets.version`;
- audit log для `CREATE_TICKET`, `ASSIGN_TICKET`, `CHANGE_STATUS`, `ADD_COMMENT`;
- представление `ticket_details`;
- `docker-compose.yml` с volume и переменными окружения.

## Проверка SQL

```sql
SELECT * FROM tickets ORDER BY id DESC;

SELECT * FROM ticket_comments ORDER BY ticket_id, created_at;

SELECT * FROM ticket_status_history ORDER BY ticket_id, changed_at;

SELECT t.id,
       t.title,
       t.status,
       customer.name AS customer,
       assignee.name AS assignee,
       c.name AS category
FROM tickets t
JOIN users customer ON customer.id = t.customer_id
LEFT JOIN users assignee ON assignee.id = t.assignee_id
LEFT JOIN categories c ON c.id = t.category_id
ORDER BY t.id;
```

## Диагностика

| Симптом | Что проверить |
|---|---|
| `Connection refused` | контейнер запущен, порт `5439` |
| `password authentication failed` | `DB_PASSWORD`, роль `helpdesk_app` |
| `database helpdesk_db does not exist` | `sql/init.sql` выполнился при создании volume |
| `No suitable driver` | зависимость `org.postgresql:postgresql` в `pom.xml` |
| `relation ... does not exist` | подключение к `helpdesk_db`, а не к `postgres` |
| `invalid input value for enum` | значения Java enum и PostgreSQL enum совпадают |
| `violates foreign key constraint` | существует ли `customer_id`/`assignee_id`/`category_id` |
| данные не появились после перезапуска | проверьте, что не сбрасывали volume и смотрите в `helpdesk_db` |

## Пароль и Git

Пароль БД не хранится в Java-коде. `DatabaseConfig` читает `DB_PASSWORD` из окружения. Учебный пароль `helpdesk_app` задан только в `sql/init.sql` и `docker-compose.yml` для локального стенда; в реальном проекте его передают через секреты/переменные окружения.
