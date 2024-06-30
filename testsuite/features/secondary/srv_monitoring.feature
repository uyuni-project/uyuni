# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature is a dependency for:
# - features/secondary/min_monitoring.feature
# - features/secondary/min_rhlike_monitoring.feature
# - features/secondary/min_deblike_monitoring.feature
#
# This feature depends on:
# - sumaform: as it is configuring monitoring to be enabled after deployment
#
# This feature can cause failures in the following features:
# - features/secondary/min_monitoring.feature
# If this feature fails,
# it could let the monitoring feature disabled for the SLE minion
# - features/secondary/min_rhlike_monitoring.feature
# If this feature fails,
# it could let the monitoring feature disabled for the Red Hat-like minion
# - features/secondary/min_deblike_monitoring.feature
# If this feature fails,
# it could let the monitoring feature disabled for the Debian-like minion

@skip_if_containerized_server
@skip_if_github_validation
@scope_monitoring
Feature: Disable and re-enable monitoring of the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
  # This assumes that monitoring is enabled via sumaform
  Scenario: Disable monitoring from the UI
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    And I click on "Disable"
    And I wait until button "Disable" becomes enabled
    Then I should see a "Monitoring disabled successfully." text
    And I should see a list item with text "System" and a failing bullet
    And I should see a list item with text "PostgreSQL database" and a failing bullet
    And I should see a list item with text "Server self monitoring" and a warning bullet
    And I should see a list item with text "Taskomatic (Java JMX)" and a warning bullet
    And I should see a list item with text "Tomcat (Java JMX)" and a warning bullet
    And I should see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text

  Scenario: Restart spacewalk services to apply config changes after disabling monitoring
    When I restart the spacewalk service

  Scenario: Check that monitoring is disabled
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    Then I should see a "Enable" button
    And I should see a "Disable" button
    And I should see a list item with text "System" and a failing bullet
    And I should see a list item with text "PostgreSQL database" and a failing bullet
    And I should see a list item with text "Server self monitoring" and a failing bullet
    And I should see a list item with text "Taskomatic (Java JMX)" and a failing bullet
    And I should see a list item with text "Tomcat (Java JMX)" and a failing bullet
    And I should not see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 0" on server
    And file "/usr/lib/systemd/system/tomcat.service.d/jmx.conf" should not exist on server
    And file "/usr/lib/systemd/system/taskomatic.service.d/jmx.conf" should not exist on server
    And port "3333" should be closed
    And port "3334" should be closed
    And port "5556" should be closed
    And port "5557" should be closed

  Scenario: Enable monitoring from the UI
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    And I click on "Enable"
    And I wait until button "Enable" becomes enabled
    Then I should see a "Monitoring enabled successfully." text
    And I should see a list item with text "System" and a success bullet
    And I should see a list item with text "PostgreSQL database" and a success bullet
    And I should see a list item with text "Server self monitoring" and a pending bullet
    And I should see a list item with text "Taskomatic (Java JMX)" and a pending bullet
    And I should see a list item with text "Tomcat (Java JMX)" and a pending bullet
    And I should see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text

  Scenario: Restart spacewalk services to apply config changes after enabling monitoring
    When I restart the spacewalk service

  Scenario: Check that monitoring is enabled
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    Then I should see a "Enable" button
    And I should see a "Disable" button
    And I should see a list item with text "System" and a success bullet
    And I should see a list item with text "PostgreSQL database" and a success bullet
    And I should see a list item with text "Server self monitoring" and a success bullet
    And I should see a list item with text "Taskomatic (Java JMX)" and a success bullet
    And I should see a list item with text "Tomcat (Java JMX)" and a success bullet
    And I should not see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 1" on server
    And file "/usr/lib/systemd/system/tomcat.service.d/jmx.conf" should contain "jmx_prometheus_javaagent.jar=5556" on server
    And file "/usr/lib/systemd/system/taskomatic.service.d/jmx.conf" should contain "jmx_prometheus_javaagent.jar=5557" on server
    And port "3333" should be closed
    And port "3334" should be closed
    And port "5556" should be open
    And port "5557" should be open
