#!/bin/bash

if [[ ! -z "$TZ" ]]; then
    timedatectl set-timezone $TZ
fi
