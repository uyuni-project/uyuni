#!/bin/sh

HELM_UNITTEST=helm-unittest
command -v $HELM_UNITTEST 2>/dev/null 1>&2
if test $? -ne 0; then
    helm plugin list | grep -q unittest
    if test $? -ne 0; then
        echo "neither helm-unittest nor helm's unittest plugin could be found"
        exit 1
    fi
    HELM_UNITTEST="helm unittest"
fi

HELM_SCHEMA=helm-schema
command -v $HELM_SCHEMA 2>/dev/null 1>&2
if test $? -ne 0; then
    helm plugin list | grep -q schema
    if test $? -ne 0; then
        echo "neither helm-schema nor helm's schema plugin could be found"
        exit 1
    fi
    HELM_SCHEMA="helm schema"
fi

set -e

echo -e "\n==> $HELM_SCHEMA\n"
$HELM_SCHEMA -n -p -r && echo "Done"

echo -e "\n==> helm-docs\n"
output=$(helm-docs -s file -z '^services\.[^.]*\..*$' -z '^volumes\.[^.]*\..*$' -x 2>&1)
if echo "$output" | grep -q "Error parsing information"; then
  echo "$output"
  exit 1
else
  echo "Done"
fi


echo -e "\n==> $HELM_UNITTEST\n"
$HELM_UNITTEST .

# helm template doesn't validate, do it with lint
helm lint . --set global.fqdn=test.local --set hubAPI.enable=true --set coco.replicas=3 --set saline.enable=true

echo -e "\n==> kubeconform\n"
# The remote schema location is to validate the Traefik CRDs.
helm template . --set global.fqdn=test.local --set hubAPI.enable=true --set coco.replicas=3 --set saline.enable=true | \
         kubeconform -summary -strict \
           -schema-location default \
           -schema-location 'https://raw.githubusercontent.com/datreeio/CRDs-catalog/main/{{.Group}}/{{.ResourceKind}}_{{.ResourceAPIVersion}}.json'

# Check the API Gateway resources
helm template . --set global.fqdn=test.local --set hubAPI.enable=true --set coco.replicas=3 --set saline.enable=true --set gateway.enable=true --set gateway.class=gw | \
         kubeconform -summary -strict \
           -schema-location default \
           -schema-location 'https://raw.githubusercontent.com/datreeio/CRDs-catalog/main/{{.Group}}/{{.ResourceKind}}_{{.ResourceAPIVersion}}.json'

echo -e "\n==> Checking for modified files\n"

set +e

git diff --exit-code README.md values.schema.json
if test $? -ne 0; then
    echo -e "\nHelm documentation or schema is out of date. Run helm-docs and helm-schema to fix it.\n"
    exit 1
else
    echo -e "OK\n"
fi
