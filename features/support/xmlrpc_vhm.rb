# COPYRIGHT 2015 SUSE LLC

require_relative 'xmlrpctest'

# vhm namespace
class XMLRPCVHMTest < XMLRPCBaseTest
  def listAvailableVirtualHostGathererModules
    @connection.call('virtualhostmanager.listAvailableVirtualHostGathererModules', @sid) || []
  end

  def listVirtualHostManagers
    @connection.call('virtualhostmanager.listVirtualHostManagers', @sid) || []
  end

  def getModuleParameters(module_name)
    @connection.call('virtualhostmanager.getModuleParameters', @sid, module_name) || {}
  end

  def getDetail(label)
    @connection.call('virtualhostmanager.getDetail', @sid, label) || {}
  end

  def create(label, module_name, parameter)
    @connection.call('virtualhostmanager.create', @sid, label, module_name, parameter)
  end

  def delete(label)
    @connection.call('virtualhostmanager.delete', @sid, label)
  end
end
