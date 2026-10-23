# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Loaded instead of features/support by .github/workflows/cucumber_dry_run.yml.
# A dry run only needs the step definitions, but features/support/env.rb connects to the infrastructure
# when it is loaded. Cucumber runs every file under a "support" directory first, so this stub goes first.

# Stub of the helper in features/support/commonlib.rb: skips the one load-time call in cobbler_steps.rb
# that would otherwise open an SSH connection.
def uyuni_not_installed?
  true
end
