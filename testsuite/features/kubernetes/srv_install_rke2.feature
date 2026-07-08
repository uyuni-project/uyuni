# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
@rke2
@no_user_creation
Feature: Install RKE2 on transactional systems

  Scenario: Check that RKE2 configuration
    Then the environment variable "RKE2_VERSION" is set on "server"
    And the environment variable "CERT_MANAGER_VERSION" is set on "server"
    And the environment variable "CERT_MANAGER_NAMESPACE" is set on "server"
    And the environment variable "TRAEFIK_FILE" is set on "server"
    And the environment variable "LOCAL_PATH_STORAGE_FILE" is set on "server"
    And the environment variable "LOCAL_PATH_PROVISIONER_STORAGE_FILE" is set on "server"
    And the environment variable "LOCAL_PATH" is set on "server"
    And the environment variable "LOCAL_PATH_NAMESPACE" is set on "server"
    And the environment variable "SELF_SIGNED_PATH" is set on "server"
    And the environment variable "HELM_CHART_DIRECTORY" is set on "server"
    And the environment variable "KUBECONFIG" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "server"

  Scenario: Install RKE2 via RPM method
    When I run "curl -sfL https://get.rke2.io | sudo INSTALL_RKE2_VERSION='$RKE2_VERSION' INSTALL_RKE2_METHOD=rpm sh -" on "server" outside the container

  Scenario: Reboot the server to activate the RKE2 transaction
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 server service
    When I enable the "rke2-server" service on "server"
    And I start the "rke2-server" service on "server"
    And I wait until "rke2-server" service is active on "server"
    Then service "rke2-server" is enabled on "server"
    And service "rke2-server" is active on "server"

  Scenario: Create symlinks for RKE2 tools
    When I run "ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/crictl /usr/local/bin/crictl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/ctr /usr/local/bin/ctr" on "server"

## Install helm
  Scenario: Install Helm
    When I run "curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-4 | bash" on "server" outside the container

  Scenario: Install cert-manager and trust-manager
    When I run "helm install cert-manager oci://quay.io/jetstack/charts/cert-manager --version $CERT_MANAGER_VERSION --namespace $CERT_MANAGER_NAMESPACE --create-namespace --set crds.enabled=true --timeout 10m0s --wait" on "server" outside the container
    Then I wait until "cert-manager" helm chart is deployed in namespace "cert-manager" on "server"
    When I run "helm upgrade trust-manager oci://quay.io/jetstack/charts/trust-manager --install --namespace $CERT_MANAGER_NAMESPACE --wait" on "server" outside the container
    Then I wait until "trust-manager" helm chart is deployed in namespace "cert-manager" on "server"

## Install Traefik

  Scenario: Install Traefik
    When I apply the RKE2 YAML file "$TRAEFIK_FILE" on "server"

## Set local-path-provisioner
  Scenario: Install local path provisioner
    When I apply the RKE2 YAML file "$LOCAL_PATH_STORAGE_FILE" on "server"
    And I set "$LOCAL_PATH_PROVISIONER_STORAGE_FILE" storage class as default on "server"
    And I run "mkdir -p $LOCAL_PATH" on "server" outside the container
    And I run "restorecon -R -v $LOCAL_PATH" on "server" outside the container
    And I run "kubectl delete pods --all -n $LOCAL_PATH_NAMESPACE" on "server" outside the container

## Install MLM/Uyuni
  Scenario: Install Uyuni
    When I run "cd $SELF_SIGNED_PATH && helm dependencies build" on "server" outside the container
    And I run "cd $SELF_SIGNED_PATH && helm upgrade --install uyuni ./selfsigned -f ./selfsigned/values.yaml -n uyuni" on "server" outside the container
