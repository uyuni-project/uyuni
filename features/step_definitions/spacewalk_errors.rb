# Copyright (c) 2016 SUSE Linux
# Licensed under the terms of the MIT license.

Then(/^I control that up2date logs on client under test contains no Traceback error$/) do
  cmd = "if grep \"Traceback\" /var/log/up2date ; then exit 1; else exit 0; fi"
  _out, code = $client.run(cmd)
  raise "error found, check the client up2date logs" if code.nonzero?
end
