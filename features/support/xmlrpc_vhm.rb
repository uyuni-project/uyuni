# COPYRIGHT 2015 SUSE LLC

require_relative 'xmlrpctest'

class XMLRPCVHMTest < XMLRPCBaseTest
  def listAvailableVirtualHostGathererModules
    @connection.call('virtualhostmanager.listAvailableVirtualHostGathererModules', @sid) || []
  end

  def listVirtualHostManagers()
    @connection.call('virtualhostmanager.listVirtualHostManagers', @sid) || []
  end

  def getModuleParameters(moduleName)
    @connection.call('virtualhostmanager.getModuleParameters', @sid, moduleName) || {}
  end

  def getDetail(label)
    @connection.call('virtualhostmanager.getDetail', @sid, label) || {}
  end

  def create(label, moduleName, parameter)
    @connection.call('virtualhostmanager.create', @sid, label, moduleName, parameter)
  end

  def delete(label)
    @connection.call('virtualhostmanager.delete', @sid, label)
  end
end
