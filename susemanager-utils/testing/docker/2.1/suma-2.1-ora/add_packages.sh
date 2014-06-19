#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run oracle server inside of the container
zypper in -y bc \
             oracle-server \
             oracle-instantclient11_2-basic \
             oracle-instantclient11_2-sqlplus \
             oracle-lib-compat \
             perl-DBD-ODBC \
             perl-DBD-Oracle

