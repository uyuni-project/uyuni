# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Tracks which identity is authenticated against which host in a multi-server (hub +
# peripherals) test run.
#
# This replaced a single flat $current_user/$current_password pair that was only ever
# correct because every test used to run against one server. Once peripherals (server2,
# server3, ...) arrived, an identity established on the hub's web UI was silently reused
# to authenticate against a peripheral's own, separate user database, since nothing
# tracked *which* host an identity belonged to.
class Credentials
  @by_host = { 'server' => %w[admin admin] }

  class << self
    # Records the identity now logged into the web UI, for whichever host the UI is
    # currently pointed at ($current_ui_host). Used by UI-driven login steps, which know
    # who just logged in but not which host string to key it under -- that's tracked
    # separately by switch_to_server/using_server.
    #
    # @param user [String] The username just logged in as.
    # @param password [String] The password used to log in.
    def login_as(user, password)
      @by_host[resolve_current_host] = [user, password]
    end

    # Returns the [user, password] known for the given host, defaulting to admin/admin
    # for any host nothing has explicitly logged into (e.g. a peripheral, by default).
    #
    # @param host [String] The target host ('server', 'server2', 'server3', ...).
    # @return [Array(String, String)] The [user, password] pair for that host.
    def for(host)
      @by_host.fetch(host, %w[admin admin])
    end

    # Returns the [user, password] for whichever host the web UI is currently pointed at.
    # For callers that mean "whoever is logged in right now" rather than a specific host.
    #
    # @return [Array(String, String)] The [user, password] pair for the current UI host.
    def current
      self.for(resolve_current_host)
    end

    private

    # $current_ui_host is only set inside the scenario After hook and switch_to_server/
    # using_server, so it's nil before either has ever run (e.g. a run's first scenario,
    # before its own After hook fires). Default that to the hub, same as its own reset value.
    def resolve_current_host
      $current_ui_host || 'server'
    end
  end
end
