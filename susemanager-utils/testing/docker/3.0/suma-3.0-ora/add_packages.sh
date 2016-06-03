#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run oracle server inside of the container
zypper in -y bc \
             curl \
             oracle-instantclient12.1-basic \
             oracle-instantclient12.1-sqlplus \
             oracle-lib-compat \
             perl-DBD-ODBC \
             perl-DBD-Oracle \
             cx_Oracle

