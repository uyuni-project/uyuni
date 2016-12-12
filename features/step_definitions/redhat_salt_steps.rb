# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Then(/^i add redhat\-minion$/) do
  master = "#master: salt/master:  #{$server_ip}"
  cmd = "sed -i -e 's/#{master}/' /etc/salt/minion"
  $rh_minion.run(cmd)
  $rh_minion.run("systemctl start salt-minion")
  $rh_minion.run("systemctl enable salt-minion")
  $rh_minion.run("systemctl status salt-minion")
  $rh_minion.run("systemctl restart salt-minion")
  # accept key in master:q
  sleep 10
  $server.run("salt-key -y --accept #{$rh_minion_fullhostname}")
end
