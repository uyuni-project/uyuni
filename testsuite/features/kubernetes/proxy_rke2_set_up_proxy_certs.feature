# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Install MLM proxy on RKE2

  Scenario: Create uyuni namespace in proxy
    When I create namespace "$PROXY_NAMESPACE" on "proxy"

  Scenario: Gen certificates for proxy
    When I copy "/root/proxy-gen-certs.yaml" from "proxy" to "server" via scp in the path "/root/proxy-gen-certs.yaml"
    And I apply the file "/root/proxy-gen-certs.yaml" with kubectl on "server"

  Scenario: Perform a key exchange between server and proxy cluster
    When I export the proxy certificate secret from the RKE2 server to "/root/proxy_secret.yaml"
    And I copy "/root/proxy_secret.yaml" from "server" outside the container to "proxy" via scp in the path "/root/proxy_secret.yaml"
    And I import the proxy certificate secret from "/root/proxy_secret.yaml" into the RKE2 proxy

  Scenario: Gen proxy configuration
    When I generate the MLM proxy configuration archive on the RKE2 server at "/root/config.tar.gz"
    And I copy "/root/config.tar.gz" from "server" outside the container to "proxy" via scp in the path "/root/config.tar.gz"

  Scenario: Copy uyuni ca
    When I export the Uyuni CA from the RKE2 server to "/root/root-ca.crt"
    And I copy "/root/root-ca.crt" from "server" outside the container to "proxy" via scp in the path "/root/root-ca.crt"
    And I import the Uyuni CA from "/root/root-ca.crt" into the RKE2 proxy
