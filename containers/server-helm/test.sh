#!/bin/sh

# Requires to install helm unittest plugin:
#   helm plugin install --verify=false https://github.com/helm-unittest/helm-unittest
helm unittest .

# Requires to install kubeconform:
#   zypper in kubeconform
# The remote schema location is to validate the Traefik CRDs.
helm template . --set global.fqdn=test.local --set hubAPI.enable=true --set coco.replicas=3 --set saline.enable=true | \
         kubeconform -summary -strict \
           -schema-location default \
           -schema-location 'https://raw.githubusercontent.com/datreeio/CRDs-catalog/main/{{.Group}}/{{.ResourceKind}}_{{.ResourceAPIVersion}}.json'
