#!/bin/bash

docker rm postgres
docker volume rm pg-data

docker volume create pg-data

docker run --name postgres --env-file=variables.list --volume pg-data:/var/lib/pgsql/data -p 5432:5432/tcp opensuse/uyuni/server/postgres:1.0
