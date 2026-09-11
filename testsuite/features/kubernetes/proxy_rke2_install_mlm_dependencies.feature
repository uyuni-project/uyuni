# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
@rke2
@no_user_creation
Feature: Install MLM dependencies on RKE2

  Scenario: Check the RKE2 configuration
    And the environment variable "CERT_MANAGER_VERSION" is set on "proxy"
    And the environment variable "CERT_MANAGER_NAMESPACE" is set on "proxy"
    And the environment variable "TRAEFIK_FILE" is set on "proxy"
    And the environment variable "LOCAL_PATH_PROVISIONER_PATH" is set on "proxy"
    And the environment variable "LOCAL_PATH_PROVISIONER_STORAGE_CLASS" is set on "proxy"
    And the environment variable "LOCAL_PATH" is set on "proxy"
    And the environment variable "LOCAL_PATH_NAMESPACE" is set on "proxy"
    And file "/etc/rancher/rke2/config.yaml" should exist on "proxy"

  ## Install helm
  Scenario: Install Helm
    When I run "set -o pipefail; curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-4 | bash" on "proxy"

  Scenario: Install cert-manager and trust-manager
    When I run "helm upgrade --install cert-manager oci://quay.io/jetstack/charts/cert-manager --version $CERT_MANAGER_VERSION --namespace $CERT_MANAGER_NAMESPACE --create-namespace --set crds.enabled=true --timeout 10m0s --wait" on "proxy"
    Then I wait until "cert-manager" helm chart is deployed in namespace "cert-manager" on "proxy"
    When I run "helm upgrade --install trust-manager oci://quay.io/jetstack/charts/trust-manager --namespace $CERT_MANAGER_NAMESPACE --wait" on "proxy"
    Then I wait until "trust-manager" helm chart is deployed in namespace "cert-manager" on "proxy"

  ## Install Traefik
  Scenario: Install Traefik
    When I apply the RKE2 YAML file "$TRAEFIK_FILE" on "proxy"

  ## Set local-path-provisioner
  Scenario: Install local path provisioner
    When I apply the RKE2 YAML file "$LOCAL_PATH_PROVISIONER_PATH" on "proxy"
    And I set "$LOCAL_PATH_PROVISIONER_STORAGE_CLASS" storage class as default on "proxy"
    And I run "mkdir -p $LOCAL_PATH" on "proxy"
    And I run "restorecon -R -v $LOCAL_PATH" on "proxy"
    And I run "kubectl delete pods --all -n $LOCAL_PATH_NAMESPACE" on "proxy"
