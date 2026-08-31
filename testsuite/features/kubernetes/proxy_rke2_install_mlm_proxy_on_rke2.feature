# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
@transactional_server
Feature: Install MLM proxy on RKE2

  Scenario: Check the RKE2 proxy configuration
    And the environment variable "PYTHON_HELM_CHART_PATH" is set on "proxy"
    And the environment variable "HELM_CHART_DIRECTORY" is set on "proxy"
    And the environment variable "SELF_SIGNED_PATH" is set on "proxy"
    And the environment variable "VALUES_YAML_PATH" is set on "proxy"
    And the environment variable "HELM_CHART_NAME" is set on "proxy"
    And the environment variable "HELM_CHART_URL" is set on "proxy"
    And the environment variable "DEVEL_FLAG" is set on "proxy"
    And the environment variable "PROXY_NAMESPACE" is set on "proxy"
    And file "/etc/rancher/rke2/config.yaml" should exist on "proxy"

  Scenario: Create helm chart directory on proxy
    When I run "mkdir -p $SELF_SIGNED_PATH" on "proxy"

  Scenario: Update OCI app version for proxy
    When I run "python3 $PYTHON_HELM_CHART_PATH -o $HELM_CHART_URL/$HELM_CHART_NAME --chart-file $SELF_SIGNED_PATH/Chart.yaml $DEVEL_FLAG" on "proxy"

  Scenario: Build helm dependencies on proxy
    When I run "cd $SELF_SIGNED_PATH && helm dependencies build" on "proxy"

  Scenario: Copy and uncompress proxy config tarball
    When I run "cp -r /root/config.tar.gz $HELM_CHART_DIRECTORY" on "proxy"
    And I run "tar -xf $HELM_CHART_DIRECTORY/config.tar.gz -C $HELM_CHART_DIRECTORY/" on "proxy"

  Scenario: Install uyuni proxy on Kubernetes
    When I run "helm upgrade --install uyuni-proxy $SELF_SIGNED_PATH -f $VALUES_YAML_PATH -n $PROXY_NAMESPACE --set-file global.ssh=$HELM_CHART_DIRECTORY/ssh.yaml --set-file global.config=$HELM_CHART_DIRECTORY/config.yaml --set-file global.httpd=$HELM_CHART_DIRECTORY/httpd.yaml" on "proxy"
