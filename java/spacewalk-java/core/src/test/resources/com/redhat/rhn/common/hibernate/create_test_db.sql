DROP TABLE IF EXISTS persist_test CASCADE;

DROP SEQUENCE IF EXISTS persist_sequence;

CREATE SEQUENCE persist_sequence;

CREATE TABLE persist_test (
    id                  NUMERIC CONSTRAINT persist_test_pk PRIMARY KEY,
    foobar              VARCHAR(32),
    test_column         VARCHAR(5),
    pin                 NUMERIC,
    hidden              VARCHAR(32),
    additional_data     JSONB,
    parent_id           NUMERIC,
    created             TIMESTAMP WITH TIME ZONE,
    modified            TIMESTAMP WITH TIME ZONE
);

INSERT INTO persist_test (foobar, id) VALUES ('Blarg', NEXTVAL('persist_sequence'));
INSERT INTO persist_test (foobar, id) VALUES ('duplicate', NEXTVAL('persist_sequence'));
INSERT INTO persist_test (foobar, id) VALUES ('duplicate', NEXTVAL('persist_sequence'));
INSERT INTO persist_test (foobar, hidden, id) VALUES ('duplicate', 'xxxxx', NEXTVAL('persist_sequence'));
INSERT INTO persist_test (foobar, id) VALUES ('vito', NEXTVAL('persist_sequence'));

INSERT INTO persist_test (foobar, id, parent_id) VALUES
    ('sonny', NEXTVAL('persist_sequence'), (SELECT id FROM persist_test WHERE foobar='vito'));

INSERT INTO persist_test (foobar, id, parent_id) VALUES
    ('fredo', NEXTVAL('persist_sequence'), (SELECT id FROM persist_test WHERE foobar='vito'));

INSERT INTO persist_test (foobar, id, parent_id) VALUES
    ('michael', NEXTVAL('persist_sequence'), (SELECT id FROM persist_test WHERE foobar='vito'));

INSERT INTO persist_test (foobar, id, additional_data) VALUES
    ('json', NEXTVAL('persist_sequence'), '{"foo": "bar", "number": 42, "array": [1, 2, 3], "tag": null, "updated": "2024-06-01T12:00:00Z"}');
