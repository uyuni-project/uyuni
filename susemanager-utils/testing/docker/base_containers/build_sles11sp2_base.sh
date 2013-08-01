#!/bin/bash

# exit if any statement returns a non true value
set -e

echo "Ensure you have kiwi >= v5.05.26 installed, you can find it inside of the virtualization:appliances project on OBS."

echo "Creating the lxc container using kiwi"
sudo /usr/sbin/kiwi --prepare sles11s_sp2_base --root sles11_sp2_rootfs --force-new-root

echo "Compressing the lxc container root"
sudo tar cpf sles11_sp2_base.tar -C sles11_sp2_rootfs .

echo "You can import the container locally doing:"
echo "  docker import - sles11_sp2_base < sles11_sp2_base.tar"

echo "Checkout README.md to learn how to push this container to a custom registry."
