#!/bin/sh

# Requires to install helm unittest plugin:
#   helm plugin install --verify=false https://github.com/helm-unittest/helm-unittest
helm unittest .

# Requires to install kubeconform:
#   zypper in kubeconform
# The remote schema location is to validate the Traefik CRDs.
helm template . --set global.config='max_cache_size_mb: 2048
server: parent.example.org
proxy_fqdn: proxy.example.org
email: foo@example.org' \
    --set global.ssh=ssh --set global.httpd=httpd | \
         kubeconform -summary -strict \
           -schema-location default \
           -schema-location 'https://raw.githubusercontent.com/datreeio/CRDs-catalog/main/{{.Group}}/{{.ResourceKind}}_{{.ResourceAPIVersion}}.json'
