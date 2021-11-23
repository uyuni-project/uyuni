#!/bin/bash

podman ps | grep proxy- | awk '{print $1}' | xargs -l1 podman kill