INSERT INTO users (email, name)
VALUES ('1@1.ww', 'Author')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, name)
VALUES ('2@2.ww', 'Liker')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO categories (name)
VALUES ('Тестовая категория')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO events (
    annotation, category_id, description, event_date, initiator_id,
    lat, lon, paid, participant_limit, request_moderation,
    state, title, created_on, published_on
) VALUES (
             'Краткое описание',
             1,
             'Полное описание',
             '2020-01-01 12:00:00',
             1,
             55.754167,
             37.620000,
             true,
             10,
             false,
             'PUBLISHED',
             'Событие в прошлом',
             '2019-12-31 23:59:59',
             '2020-01-01 00:00:01'
    ) ON CONFLICT DO NOTHING;