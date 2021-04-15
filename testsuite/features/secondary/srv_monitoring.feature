# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_monitoring
Feature: Enable and disable monitoring of the server

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
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 0" on server
    And file "/etc/sysconfig/tomcat" should not contain "Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server
    And file "/etc/rhn/taskomatic.conf" should not contain "Dcom.sun.management.jmxremote.port=3334 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server

  Scenario: Restart spacewalk services to apply config changes after disabling monitoring
    When I restart the spacewalk service

  Scenario: Check that monitoring is disabled using the UI
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
    And file "/etc/rhn/rhn.conf" should contain "prometheus_monitoring_enabled = 1" on server
    And file "/etc/sysconfig/tomcat" should contain "Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server
    And file "/etc/rhn/taskomatic.conf" should contain "Dcom.sun.management.jmxremote.port=3334 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=" on server

  Scenario: Restart spacewalk services to apply config changes after enabling monitoring
    When I restart the spacewalk service

  Scenario: Check that monitoring is enabled using the UI
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
