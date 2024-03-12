##!/bin/bash
#set -xe
#echo proxyproductuuid > /tmp/proxy_product_uuid

# OPTION 1: Run a proxy container from an image already built on OBS
#           Passing on it the config.yaml file with all that is needed to run the proxy

#sudo -i podman run --privileged --rm -d --network uyuni-network-1 -v /tmp/proxy_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/test-all-in-one:/tmp --name proxy -h proxy >>>ADD HERE THE PROXY IMAGE FROM OBS<<<<

# OPTION 2: Build a proxy container image from a new Dockerfile and run it
#           Passing on it the config.yaml file with all that is needed to run the proxy

#sudo -i podman run --privileged --rm -d --network uyuni-network-1 -v /tmp/proxy_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/test-all-in-one:/tmp --name proxy -h proxy ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION