# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Configure MLM on the RKE2 proxy
  In order to install MLM on the RKE2 proxy
  As the system administrator
  I want the proxy deployment configuration to be available

  Scenario: Verify the proxy deployment configuration
    Then the environment variable "CERT_MANAGER_VERSION" is set on "proxy"
    And the environment variable "CERT_MANAGER_NAMESPACE" is set on "proxy"
    And the environment variable "TRAEFIK_FILE" is set on "proxy"
    And the environment variable "LOCAL_PATH_PROVISIONER_PATH" is set on "proxy"
    And the environment variable "LOCAL_PATH_PROVISIONER_STORAGE_CLASS" is set on "proxy"
    And the environment variable "LOCAL_PATH_PROVISIONER_FILE" is set on "proxy"
    And the environment variable "LOCAL_PATH_NAMESPACE" is set on "proxy"
    And the environment variable "PYTHON_HELM_CHART_PATH" is set on "proxy"
    And the environment variable "HELM_CHART_DIRECTORY" is set on "proxy"
    And the environment variable "SELF_SIGNED_PATH" is set on "proxy"
    And the environment variable "VALUES_YAML_PATH" is set on "proxy"
    And the environment variable "HELM_CHART_NAME" is set on "proxy"
    And the environment variable "HELM_CHART_URL" is set on "proxy"
    And the environment variable "DEVEL_FLAG" is set on "proxy"
    And the environment variable "PROXY_NAMESPACE" is set on "proxy"
    And the environment variable "PROXY_DEPLOY_NAME" is set on "proxy"
    And the environment variable "PROXY_NAME_CERT" is set on "server"
    And the environment variable "PROXY_NAME_CERT" is set on "proxy"
    And the environment variable "PROXY_FQDN" is set on "server"
    And the environment variable "SERVER_FQDN" is set on "server"
    And the environment variable "SERVER_NAMESPACE" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "proxy"
