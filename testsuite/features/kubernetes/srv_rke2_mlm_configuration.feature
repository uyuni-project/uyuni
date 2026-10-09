# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Configure MLM on the RKE2 server
  In order to install MLM on the RKE2 server
  As the system administrator
  I want the server deployment configuration to be available

  Scenario: Verify the server deployment configuration
    Then the environment variable "CERT_MANAGER_VERSION" is set on "server"
    And the environment variable "CERT_MANAGER_NAMESPACE" is set on "server"
    And the environment variable "TRAEFIK_FILE" is set on "server"
    And the environment variable "LOCAL_PATH_PROVISIONER_PATH" is set on "server"
    And the environment variable "LOCAL_PATH_PROVISIONER_STORAGE_CLASS" is set on "server"
    And the environment variable "LOCAL_PATH_PROVISIONER_FILE" is set on "server"
    And the environment variable "LOCAL_PATH_NAMESPACE" is set on "server"
    And the environment variable "SERVER_NAMESPACE" is set on "server"
    And the environment variable "SCC_SECRET_NAME" is set on "server"
    And the environment variable "CC_USERNAME" is set on "server"
    And the environment variable "CC_PASSWORD" is set on "server"
    And the environment variable "PYTHON_HELM_CHART_PATH" is set on "server"
    And the environment variable "HELM_CHART_DIRECTORY" is set on "server"
    And the environment variable "HELM_CHART_URL" is set on "server"
    And the environment variable "HELM_CHART_NAME" is set on "server"
    And the environment variable "SELF_SIGNED_PATH" is set on "server"
    And the environment variable "VALUES_YAML_PATH" is set on "server"
    And the environment variable "DEVEL_FLAG" is set on "server"
    And the environment variable "SERVER_DEPLOY_NAME" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "server"

  @create_spacewalk_pv
  Scenario: Verify the Spacewalk persistent volume configuration
    Then the environment variable "SPACEWALK_PV_FILE" is set on "server"

  @create_pgsql_pv
  Scenario: Verify the PostgreSQL persistent volume configuration
    Then the environment variable "PGSQL_PV_FILE" is set on "server"
