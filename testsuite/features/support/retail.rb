# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

# This function returns the net prefix, caching it
def net_prefix
  $net_prefix = $private_net.sub(%r{\.0+/24$}, '.') if $net_prefix.nil?
  $net_prefix
end

# extract various data from Retail yaml configuration
def read_terminals_from_yaml
  name = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
  tree = YAML.load_file(name)
  tree['branches'].values[0]['terminals'].keys
end

# Extract the branch prefix from the Retail yaml configuration
def read_branch_prefix_from_yaml
  name = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
  tree = YAML.load_file(name)
  tree['branches'].values[0]['branch_prefix']
end

# determine OS image for PXE boot and terminal tests
def compute_image(host)
  # TODO: now that the terminals derive from sumaform's pxe_boot module,
  #       we could also specify the desired image as an environment variable
  case host
  when 'pxeboot_minion'
    $pxeboot_image
  when 'sle15sp6_terminal'
    'sles15sp6o'
  when 'sle15sp7_terminal'
    'sles15sp7o'
  else
    raise "Is #{host} a supported terminal?"
  end
end

# determine Kiwi profile filename for PXE boot and terminal tests
def compute_kiwi_profile_filename(host)
  image = compute_image(host)
  case image
  when 'sles15sp6o'
    'kiwi_profiles/POS_Image-JeOS7_SLES15SP6'
  when 'sles15sp7o'
    'kiwi_profiles/POS_Image-JeOS7_SLES15SP7'
  else
    raise "Is #{image} a supported image version?"
  end
end

# determine Kiwi profile name for PXE boot and terminal tests
def compute_kiwi_profile_name(host)
  image = compute_image(host)
  case image
  when 'sles15sp6o'
    'POS_Image-JeOS7_SLES15SP6'
  when 'sles15sp7o'
    'POS_Image-JeOS7_SLES15SP7'
  else
    raise "Is #{image} a supported image version?"
  end
end

# determine Kiwi profile version for PXE boot and terminal tests
def compute_kiwi_profile_version(host)
  image = compute_image(host)
  case image
  when 'sles15sp7o', 'sles15sp6o'
    '7.0.0'
  else
    raise "Is #{image} a supported image version?"
  end
end

# retrieve build host id, needed for scheduleImageBuild call
def retrieve_build_host_id
  systems = $api_test.system.list_systems
  refute_nil(systems)
  build_host_id = systems
                  .select { |s| s['name'] == get_target('build_host').full_hostname }
                  .map { |s| s['id'] }.first
  refute_nil(build_host_id, "Build host #{get_target('build_host').full_hostname} is not yet registered?")
  build_host_id
end

# determine the ipv6 and run an expect file
def execute_expect_command_proxy(host, exp_file, context)
  # convert MAC address to IPv6 link-local address
  case host
  when 'pxeboot_minion'
    mac = $pxeboot_mac
  when 'sle15sp6_terminal'
    mac = $sle15sp6_terminal_mac
  when 'sle15sp7_terminal'
    mac = $sle15sp7_terminal_mac
  end
  mac = mac.tr(':', '')
  eui64_base = "#{mac[0..5]}fffe#{mac[6..11]}"
  hex = (eui64_base.to_i(16) ^ 0x0200000000000000).to_s(16)
  interface = product == 'Uyuni' ? 'ens4' : 'eth1'
  ipv6 = "fe80::#{hex[0..3]}:#{hex[4..7]}:#{hex[8..11]}:#{hex[12..15]}%#{interface}"
  source = "#{File.dirname(__FILE__)}/../upload_files/#{exp_file}"
  dest = "/tmp/#{exp_file}"
  success = file_inject(get_target('proxy'), source, dest)
  raise ScriptError, 'File injection failed' unless success

  get_target('proxy').run("expect -f /tmp/#{exp_file} #{ipv6} #{context}")
end
