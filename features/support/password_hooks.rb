# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# we use these hooks instead of more steps because we need them to run
# regardless of whether or not the tests passed

After('@revertshortpass') do |scenario|
  changepass(scenario, 'A')
end

After('@revertgoodpass') do |scenario|
  changepass(scenario, 'GoodPass')
end

def fill_Field(field, pwd, timeout)
  fill_in field, :with => pwd
rescue
    sleep(timeout)
    begin
      fill_in field, :with => pwd
    rescue
      sleep(timeout)
      fill_in field, :with => pwd
    end
end

def changepass(scenario, password)
  # only change the password if the wrong worked.
  # (Guard clause)
  return false unless has_xpath?("//a[@href='/rhn/Logout.do']")

  signout = find(:xpath, "//a[@href='/rhn/Logout.do']")
  signout.click if signout
  # sometimes race condition,
  # Unable to find field "username" (Capybara::ElementNotFound)
  fill_Field("username", "admin", 15)
  fill_in "password", :with => password
  click_button "Sign In"
  find_link("Your Account").click
  sleep(10)
  fill_Field("desiredpassword", "admin", 5)
  fill_Field("desiredpasswordConfirm", "admin", 5)
  click_button "Update"
end
