File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCCVEAuditTest < XMLRPCBaseTest
  def populateCVEServerChannels()
    return @connection.call("audit.populateCVEServerChannels", @sid)
  end

  def listSystemsByPatchStatus(cve_identifier)
    return @connection.call("audit.listSystemsByPatchStatus", @sid, cve_identifier)
  end
end
