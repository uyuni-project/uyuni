#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "cd /testsuite && rake utils:filter_secondary /testsuite/run_sets/filter.yml /testsuite/run_sets/secondary.yml /testsuite/run_sets/secondar_f.yml "

