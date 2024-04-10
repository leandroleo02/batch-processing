CREATE TABLE IF NOT EXISTS people  (
                         person_id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
                         first_name VARCHAR(20),
                         last_name VARCHAR(20)
);