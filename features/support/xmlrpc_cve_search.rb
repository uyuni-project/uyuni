require_relative 'xmlrpctest'

class XMLRPCCVEAuditTest < XMLRPCBaseTest
  def populateCVEServerChannels
    return @connection.call("audit.populateCVEServerChannels", @sid)
  end

  def listSystemsByPatchStatus(cve_identifier)
    return @connection.call("audit.listSystemsByPatchStatus", @sid, cve_identifier)
  end
end
