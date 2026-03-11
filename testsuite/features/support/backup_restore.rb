# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Helper method to check if backup files exist in the specified directory
#
# @return [Boolean] true if backup files exist, false otherwise
def backup_exists?
  target = get_target('server')
  output, _code = target.run("find #{get_context('backup_dir')} -maxdepth 1 -type f -name '*.tar*'", runs_in_container: false)
  root_level = !output.lines.empty?
  output, _code = target.run("find #{get_context('backup_dir')} -mindepth 2 -type f -name '*.tar*'", runs_in_container: false)
  sub_level = !output.lines.empty?
  root_level && sub_level
end
