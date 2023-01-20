#!/usr/bin/env bash

############################# SETUP #############################

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
}

msg() {
  echo >&2 -e "${1-}"
}

die() {
  local msg=$1
  local code=${2-1}
  msg "$msg"
  exit "$code"
}

############################# ROOT #############################

usage_root() {
  cat <<EOF
Usage: proxy cli [-h] [-v] command [param...]

A CLI tool to control the containerized proxy in the systemd-service mode.

Available options:

-h, --help      Print this help and exit
-v, --verbose   Print script debug info
update          Updates a service with the new parameters
reset           Resets a service to the new parameters
EOF
  exit
}

parse_root_params() {
  while :; do
    case "${1-}" in
    -h | --help) usage_root ;;
    -v | --verbose) ;;
    update)
      shift
      update "$@"
      ;;
    reset)
      shift
      reset "$@"
      ;;
    ?*) die "Unknown option: $1" ;;
    *) usage_root ;;
    esac
    shift
  done
  return 0
}

############################# UPDATE branch #############################

usage_update() {
  cat <<EOF
Usage: proxy update command [param...]

Updates a service with the new parameters.

Available options:

image          Updates the configuration of a service
                  -s name of the service
                  -t image tag
                  -r registry to pull from
EOF
  exit
}

update() {
  args=("$@")
  if [ ${#args[@]} -eq 0 ]; then die "Missing script argument for update"; fi
  while :; do
    case "${1-}" in
    -h | --help) usage_update ;;
    image)
      shift
      update_image "$@"
      ;;
    *) die "Unknown option for update" ;;
    esac
  done

  exit 0
}

update_image() {

  SERVICE=''
  REGISTRY=''
  TAG=''
  while getopts "s:t:r:" opt; do
    case $opt in
    s)
      case $OPTARG in
      "httpd" | "ssh" | "squid" | "salt-broker" | "tftpd") SERVICE=$OPTARG ;;
      ?*)
        die "Unknown service name"
        ;;
      esac
      ;;
    t)
      TAG=$OPTARG
      ;;
    r)
      REGISTRY=$OPTARG
      ;;
    \?)
      usage_update
      die "Invalid option: ${OPTARG}"
      ;;
    esac
  done

  if [ -z "${REGISTRY}" ]; then die "Missing registry -r"; fi
  if [ -z "${TAG}" ]; then die "Missing tag -t"; fi
  echo "updating the service ${SERVICE} with the new registry/tag ${REGISTRY}/${TAG}"

  mkdir -p /etc/sysconfig || die "Cannot update"
  cat >/etc/sysconfig/uyuni-proxy-"${SERVICE}"-service.config <<EOF
NAMESPACE=$REGISTRY
TAG=$TAG
EOF

  systemctl daemon-reload || die "Cannot reload daemon"
  systemctl restart uyuni-proxy-"${SERVICE}"-service || die "Cannot restart restart uyuni-proxy-${SERVICE}-service"

  exit 0
}

############################# RESET branch #############################

reset() {

  SERVICE=''
  while getopts "s:" opt; do
    case $opt in
    s)
      case $OPTARG in
      "httpd" | "ssh" | "squid" | "salt-broker" | "tftpd") SERVICE=$OPTARG ;;
      ?*)
        die "Unknown service name"
        ;;
      esac
      ;;
    \?)
      die "Invalid option: ${OPTARG}"
      ;;
    esac
  done

  echo "resetting the config for service ${SERVICE} with the default parameters"
  rm -f /etc/sysconfig/uyuni-proxy-"${SERVICE}"-service.config || die "Cannot reset config"

  systemctl daemon-reload || die "Cannot reload daemon"
  systemctl restart uyuni-proxy-"${SERVICE}"-service || die "Cannot restart restart uyuni-proxy-${SERVICE}-service"

  exit 0
}

############################# entry #############################

parse_root_params "$@"
