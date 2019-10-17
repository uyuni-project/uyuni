# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Enable and disable monitoring of the server

  # This assumes that monitoring is enabled via sumaform
  Scenario: Disable monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    And I click on "Disable"
    And I wait until button "Disable" becomes enabled
    Then I should see a "Monitoring disabled successfully." text
    And I should see a list item with text "System" and bullet with style "fa-times text-danger"
    And I should see a list item with text "PostgreSQL database" and bullet with style "fa-times text-danger"
    And I should see a list item with text "Server self monitoring" and bullet with style "fa-hand-o-right text-danger"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with style "fa-hand-o-right text-danger"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with style "fa-hand-o-right text-danger"
    And I should see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 0" on server
    And file "/etc/sysconfig/tomcat" should not contain "Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server
    And file "/etc/rhn/taskomatic.conf" should not contain "Dcom.sun.management.jmxremote.port=3334 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server

  Scenario: Restart spacewalk services to apply monitoring config changes
    When I restart the spacewalk service

  Scenario: Check that monitoring is disabled using the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    Then I should see a "Enable" button
    And I should see a "Disable" button
    And I should see a list item with text "System" and bullet with style "fa-times text-danger"
    And I should see a list item with text "PostgreSQL database" and bullet with style "fa-times text-danger"
    And I should see a list item with text "Server self monitoring" and bullet with style "fa-times text-danger"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with style "fa-times text-danger"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with style "fa-times text-danger"
    And I should not see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text

  Scenario: Enable monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    And I click on "Enable"
    And I wait until button "Enable" becomes enabled
    Then I should see a "Monitoring enabled successfully." text
    And I should see a list item with text "System" and bullet with style "fa-check text-success"
    And I should see a list item with text "PostgreSQL database" and bullet with style "fa-check text-success"
    And I should see a list item with text "Server self monitoring" and bullet with style "fa-hand-o-right text-success"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with style "fa-hand-o-right text-success"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with style "fa-hand-o-right text-success"
    And I should see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 1" on server
    And file "/etc/sysconfig/tomcat" should contain "Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server
    And file "/etc/rhn/taskomatic.conf" should contain "Dcom.sun.management.jmxremote.port=3334 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server

  Scenario: Restart spacewalk services to apply monitoring config changes
    When I restart the spacewalk service

  Scenario: Check that monitoring is enabled using the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I see "Server self monitoring" text
    Then I should see a "Enable" button
    And I should see a "Disable" button
    And I should see a list item with text "System" and bullet with style "fa-check text-success"
    And I should see a list item with text "PostgreSQL database" and bullet with style "fa-check text-success"
    And I should see a list item with text "Server self monitoring" and bullet with style "fa-check text-success"
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with style "fa-check text-success"
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with style "fa-check text-success"
    And I should not see a "Restarting Tomcat and Taskomatic is needed for the configuration changes to take effect." text
