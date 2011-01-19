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

def changepass(scenario, password)
  signout = find_link("Sign Out")
  if signout
    signout.click
  end
  fill_in "username", :with => "admin"
  fill_in "password", :with => password
  click_button "Sign In"

  # only change the password if the wrong one worked
  if find_link("Sign Out")
    find_link("Your Account").click
    fill_in "desiredpassword", :with => "admin"
    fill_in "desiredpasswordConfirm", :with => "admin"
    click_button "Update"
  end
end
