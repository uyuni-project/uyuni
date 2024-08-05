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
  when 'sle12sp5_terminal'
    'sles12sp5o'
  when 'sle15sp3_terminal'
    'sles15sp3o'
  when 'sle15sp4_terminal'
    'sles15sp4o'
  else
    raise "Is #{host} a supported terminal?"
  end
end

# determine Kiwi profile filename for PXE boot and terminal tests
def compute_kiwi_profile_filename(host)
  image = compute_image(host)
  case image
  when 'sles15sp4', 'sles15sp4o'
    # 4.3 currently shares its profile with head
    product == 'Uyuni' ? 'Kiwi/POS_Image-JeOS7_uyuni' : 'Kiwi/POS_Image-JeOS7_head'
  when 'sles15sp3', 'sles15sp3o'
    raise 'Kiwi profile for 4.2 has been removed.'
  when 'sles15sp2', 'sles15sp2o'
    raise 'Kiwi profile for 4.1 has been removed.'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    # 4.3 currently shares its profile with head
    'Kiwi/POS_Image-JeOS6_head'
  else
    raise "Is #{image} a supported image version?"
  end
end

# determine Kiwi profile name for PXE boot and terminal tests
def compute_kiwi_profile_name(host)
  image = compute_image(host)
  case image
  when 'sles15sp4', 'sles15sp4o'
    # 4.3 currently shares its profile with head
    product == 'Uyuni' ? 'POS_Image_JeOS7_uyuni' : 'POS_Image_JeOS7_head'
  when 'sles15sp3', 'sles15sp3o'
    raise 'Kiwi profile for 4.2 has been removed.'
  when 'sles15sp2', 'sles15sp2o'
    raise 'Kiwi profile for 4.1 has been removed.'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    # 4.3 currently shares its profile with head
    'POS_Image_JeOS6_head'
  else
    raise "Is #{image} a supported image version?"
  end
end

# determine Kiwi profile version for PXE boot and terminal tests
def compute_kiwi_profile_version(host)
  image = compute_image(host)
  case image
  when 'sles15sp4', 'sles15sp4o'
    '7.0.0'
  when 'sles15sp3', 'sles15sp3o'
    raise 'Kiwi profile for 4.2 has been removed.'
  when 'sles15sp2', 'sles15sp2o'
    raise 'Kiwi profile for 4.1 has been removed.'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    '6.0.0'
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
