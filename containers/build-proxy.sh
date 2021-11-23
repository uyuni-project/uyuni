#!/bin/bash

export REGISTRY=registry.tf.local

for image in proxy-main proxy-salt-broker proxy-squid
do
    pushd $image
    podman build -t $image -t $REGISTRY/$image .
    podman image push --tls-verify=false $REGISTRY/$image
    popd
done
