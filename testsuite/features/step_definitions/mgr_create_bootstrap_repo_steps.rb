
Given(/^the list of distributions$/) do |table|
  @distros = table.raw.flatten
end

Then(/^calling mgr\-create\-bootstrap\-repo \-c should show no error$/) do
  @distros.each do |distro|
    command_output = sshcmd("mgr-create-bootstrap-repo -c #{distro}")[:stdout]
    refute_includes(command_output, "ERROR")
  end
end
