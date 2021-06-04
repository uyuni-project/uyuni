#!/bin/bash

pushd postgres
bash build.sh
popd

pushd postgres-init
bash build.sh
popd
