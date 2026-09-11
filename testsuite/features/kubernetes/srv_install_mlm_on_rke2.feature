# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
@rke2
@no_user_creation
Feature: Install MLM on RKE2

  Scenario: Check the RKE2 configuration
    Then the environment variable "PYTHON_HELM_CHART_PATH" is set on "server"
    And the environment variable "HELM_CHART_DIRECTORY" is set on "server"
    And the environment variable "HELM_CHART_URL" is set on "server"
    And the environment variable "HELM_CHART_NAME" is set on "server"
    And the environment variable "SELF_SIGNED_PATH" is set on "server"
    And the environment variable "VALUES_YAML_PATH" is set on "server"
    And the environment variable "HELM_CHART_NAME" is set on "server"
    And the environment variable "HELM_CHART_URL" is set on "server"
    And the environment variable "SERVER_NAMESPACE" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "server"

  Scenario: Update OCI app version
    When I run "python3 $PYTHON_HELM_CHART_PATH -o $HELM_CHART_URL/$HELM_CHART_NAME --chart-file $SELF_SIGNED_PATH/Chart.yaml $DEVEL_FLAG" on "server"

  @install_mlm_on_rke2
  Scenario: Install Uyuni
    And I run "kubectl create namespace $SERVER_NAMESPACE --dry-run=client -o yaml | kubectl apply -f -" on "server"
    And I run "cd $SELF_SIGNED_PATH && helm dependencies build" on "server"
    And I run "helm upgrade --install uyuni $SELF_SIGNED_PATH -f $VALUES_YAML_PATH -n $SERVER_NAMESPACE" on "server"
