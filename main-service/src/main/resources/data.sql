INSERT INTO users (email, name)
SELECT '1@1.ww', 'Author'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '1@1.ww');

INSERT INTO users (email, name)
SELECT '2@2.ww', 'Liker'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '2@2.ww');

INSERT INTO categories (name)
SELECT 'Тестовая категория'
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Тестовая категория');

INSERT INTO events (
    annotation, category_id, description, event_date, initiator_id,
    lat, lon, paid, participant_limit, request_moderation,
    state, title, created_on, published_on
)
SELECT
    'Краткое описание',
    (SELECT id FROM categories WHERE name = 'Тестовая категория' LIMIT 1),
    'Полное описание',
    '2020-01-01 12:00:00',
    (SELECT id FROM users WHERE email = '1@1.ww' LIMIT 1),
    55.754167, 37.620000, true, 10, false, 'PUBLISHED', 'Событие в прошлом',
    '2019-12-31 23:59:59', '2020-01-01 00:00:01'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Событие в прошлом');
