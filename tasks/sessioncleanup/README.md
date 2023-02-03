# Building

Run the `build.sh` script from this folder

# Using

Running this container requires the postgresql database to listen on all addresses.
For now the task assumes an unsecure connection 🤮.

Use the following environment variables to setup the connection:

* `PGSQL_HOST`
* `PGSQL_PORT` defaults to `5432`
* `PGSQL_USER`
* `PGSQL_PASSWORD`
* `PGSQL_DB` defaults to `spacewalk`

Those can be stored in a text file with one value definition per line (no space around `=`) and the file path passed to `podman --env-file`.

The session limit in seconds can be passed as parameter and defaults to `3600`.

A call like the following will get the task to run once and exit:

```
podman run --rm --env-file env.txt session-cleanup-go 4800
```
