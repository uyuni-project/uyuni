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

def ckUsernameField(timeout)
  fill_in "username", :with => "admin"
rescue
    sleep(timeout)
    fill_in "username", :with => "admin"
end

def changepass(scenario, password)
  # only change the password if the wrong worked.
  # (Guard clause)
  return false unless has_xpath?("//a[@href='/rhn/Logout.do']")

  signout = find(:xpath, "//a[@href='/rhn/Logout.do']")
  signout.click if signout
  # sometimes race condition,
  # Unable to find field "username" (Capybara::ElementNotFound)
  ckUsernameField(10)
  fill_in "password", :with => password
  click_button "Sign In"
  find_link("Your Account").click
  sleep(5)
  begin
    fill_in "desiredpassword", :with => "admin"
  rescue
    sleep(5)
    fill_in "desiredpassword", :with => "admin"
  end
  begin
    fill_in "desiredpasswordConfirm", :with => "admin"
  rescue
    sleep(5)
    fill_in "desiredpasswordConfirm", :with => "admin"
  end
  click_button "Update"
end
