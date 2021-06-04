#!/bin/bash

docker rm postgres-init
docker rm postgres
docker volume rm pg-data


docker volume create pg-data

docker run --name postgres-init --volume pg-data:/var/lib/pgsql/data --env-file=variables.list opensuse/uyuni/server/postgres-init

docker run --name postgres --volume pg-data:/var/lib/pgsql/data -p 5432:5432/tcp opensuse/uyuni/server/postgres
