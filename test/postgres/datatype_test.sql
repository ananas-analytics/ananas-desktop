--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;



SET default_tablespace = '';

SET default_with_oids = false;

DROP TABLE IF EXISTS datatype_test;

CREATE TABLE datatype_test (
    xml_type xml,
    description text,
    price money,
    ba    bytea,
    num3a NUMERIC(4),
    serial_type SERIAL,
    bigserial BIGSERIAL, 
    smallserial SMALLSERIAL,
    real_type real,
    double_type double precision,
    smallint_type smallint,
    integer_type integer,
    bigint_type bigint,
    decimal_type decimal, 
    numeric_type  numeric,
    character_varying_type character varying(32), 
    varchar_type varchar(32),
    character_type character(32), 
    char_type char(32),
    text_type text,
    timestamp_type timestamp,
    timestamp_with_timezone_type timestamp with time zone,
    date_type date,
    time_without_time_zone_type time without time zone,
    time_type time,
    time_with_time_zone_type time with time zone,
    interval_type interval,
    boolean_type boolean,
    bit_type BIT VARYING(5),
    inet_type inet,
    uuid_type uuid,
    json_type json,
    int_array_type  integer[],
    oid_type oid,
    range_type int4range
);

INSERT INTO datatype_test(xml_type, description, price, ba, num3a, real_type, double_type, smallint_type, integer_type, bigint_type, decimal_type, numeric_type, character_varying_type,
varchar_type, character_type, char_type, text_type, timestamp_type, timestamp_with_timezone_type, date_type, time_without_time_zone_type, time_type, time_with_time_zone_type, interval_type, 
boolean_type, bit_type, inet_type, uuid_type, json_type, int_array_type, oid_type, range_type) VALUES (
    XMLPARSE(DOCUMENT '<?xml version="1.0"?><book><title>Manual</title><chapter>...</chapter></book>'),
    'text column',
    '12.34'::float8::numeric::money,
    '\xDEADBEEF',
    3.001,
    3.001,
    4.0, 
    34,
    34,
    34, 
    34.5, 
    3.1,
    'character varying(12)',
    'varchar_type',
    'character_type',
    'char_type',
    'text_type',
    '2003-04-12 04:05:06',
    '2003-04-12 04:05:06 America/New_York',
    '2003-04-12',
    '01:00:00',
    '02:00:00',
    '02:00:00+149',
    '80 minutes'::interval,
    true,
    B'00',
    '192.168.100.128/25',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '[1, 2, "foo", null]'::json,
    '{10000, 10000, 10000, 10000}',
    11,
    int4range(15, 25)
);




