#!/bin/bash
set -e

rm /etc/products.d/baseproduct
zypper --non-interactive install sles-release less vim
ln -s SLES.prod /etc/products.d/baseproduct
