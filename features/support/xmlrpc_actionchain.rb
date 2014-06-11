File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require "xmlrpctest"
require "base64"

class XMLRPCActionChain < XMLRPCBaseTest
  def listChains()
    chains = @connection.call("actionchain.listChains", @sid)
    labels = []
    for chain in chains
      labels.push(chain['label'])
    end
    return labels
  end


  def createChain(label)
    return @connection.call("actionchain.createChain", @sid, label)
  end


  def deleteChain(label)
    return @connection.call("actionchain.deleteChain", @sid, label)
  end


  def removeAction(label, aid)
    return @connection.call("actionchain.removeAction", @sid, label, aid)
  end


  def renameChain(oldLabel, newLabel)
    return @connection.call("actionchain.renameChain", @sid, oldLabel, newLabel)
  end


  def addScriptRun(system, script, label)
    return @connection.call("actionchain.addScriptRun",
                            @sid, system, label, "root", "root", 300,
                            Base64.encode64(script))
  end


  def listChainActions(label)
    return @connection.call("actionchain.listChainActions", @sid, label)
  end


  def addSystemReboot(system, label)
    return @connection.call("actionchain.addSystemReboot", @sid, system, label)
  end


  def addPackageInstall(system, packages, label)
    return @connection.call("actionchain.addPackageInstall", @sid, system, packages, label)
  end


  def addPackageUpgrade(system, packages, label)
    return @connection.call("actionchain.addPackageUpgrade", @sid, system, packages, label)
  end


  def addPackageVerify(system, packages, label)
    return @connection.call("actionchain.addPackageVerify", @sid, system, packages, label)
  end


  def addPackageRemoval(system, packages, label)
    return @connection.call("actionchain.addPackageRemoval", @sid, system, packages, label)
  end


  def scheduleChain(label, iso8601)
    return @connection.call("actionchain.scheduleChain", @sid, label, iso8601)
  end
end
