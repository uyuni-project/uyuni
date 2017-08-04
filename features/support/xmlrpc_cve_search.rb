require_relative 'xmlrpctest'

# audit class xmlrpc
class XMLRPCCVEAuditTest < XMLRPCBaseTest
  def populateCVEServerChannels
    @connection.call('audit.populateCVEServerChannels', @sid)
  end

  def listSystemsByPatchStatus(cve_identifier)
    @connection.call('audit.listSystemsByPatchStatus', @sid, cve_identifier)
  end
end
