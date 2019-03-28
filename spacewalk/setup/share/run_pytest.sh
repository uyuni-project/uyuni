#!/bin/bash
#
# This is a temporary solution before all
# tests are running on docker.
#
# Author: bo@suse.de
#

VENV=".spacewalk-setup-env"

#
# Setup environment
#
function setup_venv () {
    if [ ! -d $VENV ]; then
	python3.6 -m venv $VENV
	source $VENV/bin/activate
	pip install --upgrade pip
	pip install six
	pip install pytest
    fi
    echo "NOTE: Virtual setup environment as $VENV"
}

#
# Ignore environment
#
function git_ignore_venv () {
    GITIGNORE=$(find -maxdepth 1 -name '.gitignore')
    if [ "$GITIGNORE" == "" ]; then
	echo $VENV >> .gitignore
    else
	ENV_ADDED=$(grep $VENV .gitignore)
	if [ "$ENV_ADDED" == "" ]; then
	    echo $VENV >> .gitignore
	fi
    fi
}

#
# Activate virtual environment
#
function activate_venv () {
    which deactivate &>/dev/null
    if [ "$?" == "1" ]; then
	source $VENV/bin/activate
    fi
}

setup_venv;
git_ignore_venv;
activate_venv;

HERE=$(cd $(dirname "${BASH_SOURCE[0]}") >/dev/null 2>&1 && pwd)
export PYTHONPATH="$PYTHONPATH:$HERE:$HERE/tests"
pytest --disable-warnings --tb=native --color=yes -svv
