#!/bin/bash

set -e

REGISTRY=registry.tf.local

IMAGES=(proxy-httpd proxy-salt-broker proxy-squid proxy-tftpd)

for image in "${IMAGES[@]}"
do
    echo "**** BUILDING $image ****"
    podman build -f $image-image/Dockerfile --build-arg BASE_IMAGE_PREFIX=registry.suse.com/ -t $image -t $REGISTRY/$image ..
done

for image in "${IMAGES[@]}"
do
    echo "**** PUSHING $image ****"
    podman image push --tls-verify=false $REGISTRY/$image
done
