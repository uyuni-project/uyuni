#!/usr/bin/bash

for folder in systems packages; do
    mkdir -p /var/spacewalk/${folder}
    chmod 0775 /var/spacewalk/${folder}
    chown wwwrun:www /var/spacewalk/${folder}
done
