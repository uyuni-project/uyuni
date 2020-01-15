# Copyright (c) 2015-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# vhm namespace
class XMLRPCVHMTest < XMLRPCBaseTest
  def list_available_virtual_host_gatherer_modules
    @connection.call('virtualhostmanager.list_available_virtual_host_gatherer_modules', @sid) || []
  end

  def list_virtual_host_managers
    @connection.call('virtualhostmanager.list_virtual_host_managers', @sid) || []
  end

  def get_module_parameters(module_name)
    @connection.call('virtualhostmanager.get_module_parameters', @sid, module_name) || {}
  end

  def get_detail(label)
    @connection.call('virtualhostmanager.get_detail', @sid, label) || {}
  end

  def create(label, module_name, parameter)
    @connection.call('virtualhostmanager.create', @sid, label, module_name, parameter)
  end

  def delete(label)
    @connection.call('virtualhostmanager.delete', @sid, label)
  end
end
