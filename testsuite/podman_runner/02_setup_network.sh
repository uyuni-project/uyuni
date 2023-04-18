#!/bin/bash
set -ex
sudo -i podman network create --ipv6 --subnet 2001:db8::/64 uyuni-network-1

