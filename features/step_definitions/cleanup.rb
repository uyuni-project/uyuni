# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

And(/^Cleanup for distro_clobber_feature$/) do
  host = $server_fullhostname
  @cli = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @cli.call('auth.login', 'admin', 'admin')
  # -------------------------------
  # cleanup kickstart profiles and distros
  distro_name = "fedora_kickstart_distro"
  @cli.call('kickstart.tree.deleteTreeAndProfiles', @sid, distro_name)
  @cli.call("auth.logout", @sid)
  # -------------------------------
  # remove not from suma managed profile
  $server.run("cobbler profile remove --name \"testprofile\"")
  # remove not from suma man. distro
  $server.run("cobbler distro remove --name \"testdistro\"")
end
