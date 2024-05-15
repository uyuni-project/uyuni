#!/bin/bash
set -ex
print_usage()
{
    echo "Usage: $0 [-s subnet]" 1>&2
    exit 1
}

# default subnet
subnet=2001:db8::/64
while getopts "s:" options; do
    case "${options}" in
        s)
            subnet=${OPTARG}
            ;;
        *)
            print_usage
            ;;
    esac
done
shift $((OPTIND-1))

sudo -i podman network create --ipv6 --subnet ${subnet} uyuni-network-1



