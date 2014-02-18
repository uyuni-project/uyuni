Feature: Pick dates
  In order to execute actions at a certain date
  As a authorized user
  I want to be able to easily pick dates

Scenario: Date picker is by default set to today
Given I am on the Systems overview page of this client
   And I follow "Remote Command" in the content area
   And I open the date picker
   Then the date picker title should be the current month and year

Scenario: Execute a command at a specific time
  Given I am on the Systems overview page of this client
   And I follow "Remote Command" in the content area
   And I enter "ls" as "Script"
   And I pick "2016-08-27" as date
   Then the date field is set to "2016-08-27"
   And the date picker is closed

