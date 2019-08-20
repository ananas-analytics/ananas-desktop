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
    description text
);

INSERT INTO datatype_test VALUES (
    XMLPARSE(DOCUMENT '<?xml version="1.0"?><book><title>Manual</title><chapter>...</chapter></book>'),
    'text column'
);
