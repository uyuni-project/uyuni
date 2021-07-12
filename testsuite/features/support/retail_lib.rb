# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'yaml'

# Retail Lib module includes a bunch of useful methods of our Retail step definitions
module RetailLib
  # extract various data from Retail yaml configuration
  def read_terminals_from_yaml
    name = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
    tree = YAML.load_file(name)
    tree['branches'].values[0]['terminals'].keys
  end

  # Extract branch prefix from Retail yaml configuration
  def read_branch_prefix_from_yaml
    name = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
    tree = YAML.load_file(name)
    tree['branches'].values[0]['branch_prefix']
  end

  # Extract server domain from Retail yaml configuration
  def read_server_domain_from_yaml
    name = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
    tree = YAML.load_file(name)
    tree['branches'].values[0]['server_domain']
  end

  # determine image for PXE boot tests
  def compute_image_filename
    case ENV['PXEBOOT_IMAGE']
    when 'sles15sp3', 'sles15sp3o'
      'Kiwi/POS_Image-JeOS7_head'
    when 'sles15sp2', 'sles15sp2o'
      # Same image version is used in case of 4.0 and 4.1
      'Kiwi/POS_Image-JeOS7_41'
    when 'sles15sp1', 'sles15sp1o'
      raise(ArgumentError, 'This is not supported image version.')
    else
      'Kiwi/POS_Image-JeOS6_head'
    end
  end

  # Compute an image name for PXE Boot
  def compute_image_name
    case ENV['PXEBOOT_IMAGE']
    when 'sles15sp3', 'sles15sp3o'
      'POS_Image_JeOS7_head'
    when 'sles15sp2', 'sles15sp2o'
      # Same kiwi image version is used in case of 4.0 and 4.1
      'POS_Image_JeOS7_41'
    when 'sles15sp1', 'sles15sp1o'
      raise(ArgumentError, 'This is not supported image version.')
    else
      'POS_Image_JeOS6_head'
    end
  end
end
