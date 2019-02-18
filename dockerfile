FROM postgres:11.1

ADD schema.sql /docker-entrypoint-initdb.d/schema.sql