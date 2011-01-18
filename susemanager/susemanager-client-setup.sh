#! /bin/bash
# ***************************************************************************
#
# Copyright (c) 2010 Novell, Inc.
# All Rights Reserved.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of version 2 of the GNU General Public License
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, contact Novell, Inc.
#
# To contact Novell about this file by physical or electronic mail,
# you may find current contact information at www.novell.com
#
# ***************************************************************************
#
# TODO: Incorporate this into rhn-bootstrap
#
set -e

function err_exit()
{
  echo "***$(basename "$0"): $@" >&2
  exit 1
}

function usage()
{
  cat <<EOF
Usage: $(basename "$0") [options] SERVER_NAME [ACTIVATION_KEY]

Download and install the software required to register this system
as a SUSE Manager client. Finally register the system. SERVER_NAME
and ACTIVATION_KEY may be passed as arguments or via the environment.

Whithout ACTIVATION_KEY registration is omitted. To do this later, call
rhn_register or rhnreg_ks.

Options:
  -f, --force		Register the system even if it is already registered
  -h, --help		Show this help message and exit
EOF
  exit 0
}

REG_ARGS=
while [ -n "$1" ]; do
  case "$1" in
  -f|--force)
    REG_ARGS="--force"
    ;;
  -h|--help)
    usage
    ;;
  *)
    break
    ;;
  esac
  shift
done

SERVER_NAME=${1:-"$SERVER_NAME"}
ACTIVATION_KEY=${2:-"$ACTIVATION_KEY"}
test -n "$SERVER_NAME"  || usage

#
# Install necessary software
#
test -x "/usr/bin/zypper" || err_exit "Can't find a sofware manager [zypper] to setup this system."
CLIENT_REPO_NAME="susemanager-client-setup"
CLIENT_REPO_URL="http://${SERVER_NAME}/pub/repositories/susemanager-client-setup"

# add client repo (always!)
CLIENT_REPO_FILE="/etc/zypp/repos.d/${CLIENT_REPO_NAME}.repo"
echo "Adding client software repository: $CLIENT_REPO_NAME"
cat <<EOF >"$CLIENT_REPO_FILE"
[$CLIENT_REPO_NAME]
name=$CLIENT_REPO_NAME
baseurl=$CLIENT_REPO_URL
enabled=1
autorefresh=0
keeppackages=0
gpgcheck=1
EOF

# refresh client repo
zypper --non-interactive --gpg-auto-import-keys refresh "$CLIENT_REPO_NAME"

# update packages
zypper --non-interactive dup --from "$CLIENT_REPO_NAME"

# check for new packages to install
function client_pkg_set() {
  cat <<EOF
spacewalk-check
spacewalk-client-setup
spacewalk-client-tools
rhncfg-actions
rhncfg-client
zypp-plugin-spacewalk
EOF
}
TO_INSTALL=""
for P in $(client_pkg_set); do
  rpm -q "$P" || TO_INSTALL="$TO_INSTALL $P"
done
test -z "$TO_INSTALL" || {
  zypper --non-interactive in --from "${CLIENT_REPO_NAME}" $TO_INSTALL
}

#
# Get and update configuration
#
echo
echo "Configure..."

function fetch_pub() {
  local _CURL="/usr/bin/curl -SksO"
  local _REMOTE="$1"
  local _LOCAL="$(basename "$_REMOTE")"
  rm -f "$_LOCAL"
  echo "...get $_LOCAL"
  $_CURL "http://${SERVER_NAME}/pub/${_REMOTE}" || {
    echo "***Error downloading ${_REMOTE}."
  }
}

# Q&D: some settings
fetch_pub "bootstrap/bootstrap.sh" || {
  err_exit "Maybe 'Bootstrap' is not yet configured?"
}
eval $(grep '^\(CLIENT_OVERRIDES\|ORG_CA_CERT\|REGISTER_THIS_BOX\|ALLOW_CONFIG_ACTIONS\|ALLOW_REMOTE_COMMANDS\)=' bootstrap.sh)
test -n "$CLIENT_OVERRIDES" -a -n "$ORG_CA_CERT" || {
  echo "***Error got empty config values."
  err_exit "Maybe 'Bootstrap' is not yet configured?"
}

# update config files
fetch_pub "bootstrap/client_config_update.py" || {
  err_exit "Maybe 'Bootstrap' is not yet configured?"
}
fetch_pub "bootstrap/${CLIENT_OVERRIDES}" || {
  err_exit "Maybe 'Bootstrap' is not yet configured?"
}

if [ -f "/etc/sysconfig/rhn/rhn_register" ] ; then
    echo "... update rhn_register config file"
    /usr/bin/python -u client_config_update.py /etc/sysconfig/rhn/rhn_register ${CLIENT_OVERRIDES}
fi
echo "... update up2date config file"
/usr/bin/python -u client_config_update.py /etc/sysconfig/rhn/up2date ${CLIENT_OVERRIDES}

#
# Check for cerificate
#
if fetch_pub "${ORG_CA_CERT}"; then
  mv "${ORG_CA_CERT}" /usr/share/rhn/
  test -e "/etc/ssl/certs/${ORG_CA_CERT},pem" || {
    test -d "/etc/ssl/certs" || mkdir -p "/etc/ssl/certs"
    ln -s "/usr/share/rhn/${ORG_CA_CERT}" "/etc/ssl/certs/${ORG_CA_CERT},pem"
    test -x /usr/bin/c_rehash && /usr/bin/c_rehash /etc/ssl/certs/
  }
else
  echo "***Error downloading the corporate public CA cert."
fi

#
# Register system at SUSE Manager server
#
if [ $REGISTER_THIS_BOX -eq 1 ]; then
  if [ -z "$ACTIVATION_KEY" ]; then
    echo "***No ACTIVATION_KEY given: Call rhn_register or rhnreg_ks register manually."
    echo "***Skipping setup..."
    exit 0
  else
    test -x "/usr/sbin/rhnreg_ks" || err_exit "Can't find application [rhnreg_ks] to register this system."
    rhnreg_ks $REG_ARGS --serverUrl="http://${SERVER_NAME}/XMLRPC" --activationkey="$ACTIVATION_KEY"
  fi
else
    echo "*.. explicitely not registering"
fi

if [ $ALLOW_CONFIG_ACTIONS -eq 1 ]; then
    if [ -x "/usr/bin/rhn-actions-control" ] ; then
        rhn-actions-control --enable-all
        rhn-actions-control --disable-run
    fi
fi

if [ $ALLOW_REMOTE_COMMANDS -eq 1 ]; then
    if [ -x "/usr/bin/rhn-actions-control" ] ; then
        rhn-actions-control --enable-run
    fi
fi

# Get the subscribed channels
zypper --non-interactive refs

if [ -x /usr/sbin/rhn-profile-sync ]; then
    /usr/sbin/rhn-profile-sync
else
    echo "Can't find [rhn-profile-sync] to update system info. Please install and rerun it."
fi
