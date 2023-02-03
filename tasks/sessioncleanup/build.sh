#!/bin/sh
podman run --rm -v $PWD:/src --workdir /src registry.suse.com/bci/golang:latest go build
podman build -t session-cleanup-go .
