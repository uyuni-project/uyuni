#!/bin/bash
set -xe
export CR=docker
${CR} exec controller-test bash -c "cd /testsuite && bundle.ruby2.5 install --gemfile Gemfile"

