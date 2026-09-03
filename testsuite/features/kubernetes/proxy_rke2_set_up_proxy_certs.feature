# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
@transactional_server
Feature: Install MLM proxy on RKE2

  Scenario: Create uyuni namespace in proxy
    When I run "kubectl create namespace $PROXY_NAMESPACE --dry-run=client -o yaml | kubectl apply -f -" on "proxy"

  Scenario: Gen certificates for proxy
    And I copy "/root/proxy-gen-certs.yaml" from "proxy" to "server" via scp in the path "/root/proxy-gen-certs.yaml"
    And I run "kubectl apply -f /root/proxy-gen-certs.yaml" on "server" outside the container

  Scenario: Perform a key exchange between server and proxy cluster
    And I run "kubectl get secret -n $SERVER_NAMESPACE -o yaml $PROXY_NAME > /root/proxy_secret.yaml" on "server" outside the container
    And I copy "/root/proxy_secret.yaml" from "server" outside the container to "proxy" via scp in the path "/root/proxy_secret.yaml"
    # Parse file
    And I run "cat /root/proxy_secret.yaml | sed -e 's/name: $PROXY_NAME/name: proxy-cert/' -e 's/namespace: $SERVER_NAMESPACE/namespace: $PROXY_NAMESPACE/' -e '/\(uid\)\|\(resourceVersion\)\|\(creationTimestamp\)\|\(cert-manager\)/d' | kubectl apply -f -" on "proxy"

  Scenario: Gen proxy configuration
    And I run "kubectl get secret $PROXY_NAME -n $SERVER_NAMESPACE -o jsonpath='{.data.ca\.crt}' | base64 -d > /root/ca.crt" on "server" outside the container
    And I run "kubectl cp /root/ca.crt $SERVER_NAMESPACE/$(kubectl get pods -n $SERVER_NAMESPACE -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}'):/ca.crt" on "server" outside the container
    And I run "kubectl get secret $PROXY_NAME -n $SERVER_NAMESPACE -o jsonpath='{.data.tls\.crt}' | base64 -d > /root/tls.crt" on "server" outside the container
    And I run "kubectl cp /root/tls.crt $SERVER_NAMESPACE/$(kubectl get pods -n $SERVER_NAMESPACE -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}'):/tls.crt" on "server" outside the container
    And I run "kubectl get secret $PROXY_NAME -n $SERVER_NAMESPACE -o jsonpath='{.data.tls\.key}' | base64 -d > /root/tls.key" on "server" outside the container
    And I run "kubectl cp /root/tls.key $SERVER_NAMESPACE/$(kubectl get pods -n $SERVER_NAMESPACE -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}'):/tls.key" on "server" outside the container
    And I run "kubectl exec $(kubectl get pods -n $SERVER_NAMESPACE -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}') -n $SERVER_NAMESPACE -- spacecmd -u admin -p admin proxy_container_config -- $PROXY_FQDN $SERVER_FQDN 2048 galaxy-noise@suse.com ca.crt tls.crt tls.key" on "server" outside the container
    And I run "kubectl cp $SERVER_NAMESPACE/$(kubectl get pods -n $SERVER_NAMESPACE -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}'):/config.tar.gz /root/config.tar.gz" on "server" outside the container
    And I copy "/root/config.tar.gz" from "server" outside the container to "proxy" via scp in the path "/root/config.tar.gz"

  Scenario: Copy uyuni ca
    And I run "kubectl get cm -n $SERVER_NAMESPACE uyuni-ca -o 'jsonpath={.data.ca\.crt}' > root-ca.crt" on "server" outside the container
    And I copy "/root/root-ca.crt" from "server" outside the container to "proxy" via scp in the path "/root/root-ca.crt"
    And I run "kubectl create configmap uyuni-ca -n $PROXY_NAMESPACE --from-file=ca.crt=root-ca.crt --dry-run=client -o yaml | kubectl apply -f -" on "proxy"


