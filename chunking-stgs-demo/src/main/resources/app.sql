
CREATE SCHEMA source_schema;
CREATE SCHEMA target_schema;

CREATE TABLE source_schema.sample_data (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    value VARCHAR(100)
);

CREATE TABLE target_schema.sample_data (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    value VARCHAR(100)
);



INSERT INTO source_schema.sample_data (name, value)
SELECT 'name_' || g, 'value_' || g
FROM generate_series(1, 1000000) g;

select count(*) from source_schema.sample_data

select count(*) from target_schema.sample_data


delete from target_schema.sample_data;

SELECT pg_size_pretty(pg_total_relation_size('source_schema.sample_data'));

SELECT pg_size_pretty(pg_total_relation_size('target_schema.sample_data'));


