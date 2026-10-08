# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

PROXY_HOSTS = %w[proxy proxy2 proxy3].freeze

# Builds the flavour suffix of a host from its detected OS, e.g. 'sles15sp7' or 'slmicro62'
#
# @param os_family [String] the detected OS family (see RemoteNode#get_os_version)
# @param os_version [String] the detected OS version, e.g. '15-SP7' or '6.2'
# @return [String] the flavour suffix, without dots or dashes in the version
def flavour_suffix(os_family, os_version)
  family = os_family.match?(/sle-micro|sl-micro|suse-microos/) ? 'slmicro' : os_family
  "#{family}#{os_version.delete('.-').downcase}"
end

# Returns the flavour suffix of one proxy host, e.g. 'sles15sp7' or 'slmicro62'
#
# @param host [String] proxy host role, e.g. 'proxy2'
# @return [String] the flavour suffix
def proxy_flavour(host)
  flavour_suffix(get_target(host).os_family, get_target(host).os_version)
end

# Returns the distinct flavours of the defined proxies, in the order of the given hosts.
# Proxies that are not defined in the current topology are skipped.
#
# @param hosts [Array<String>] proxy host roles to look at
# @return [Array<String>] one flavour suffix per distinct flavour
def proxy_flavours(hosts = PROXY_HOSTS)
  defined_hosts = hosts.select { |host| ENV_VAR_BY_HOST.key?(host) && ENV.key?(ENV_VAR_BY_HOST[host]) }
  defined_hosts.map { |host| proxy_flavour(host) }.uniq
end

# Looks up the MU repositories of a client; 'proxy_<flavour>' falls back to 'proxy'
#
# @param client [String] the key in custom_repositories.json
# @return [Hash, nil] repository name to URL, or nil if none is configured
def custom_repositories_for(client)
  $custom_repositories[client] || (client.start_with?('proxy_') ? $custom_repositories['proxy'] : nil)
end
