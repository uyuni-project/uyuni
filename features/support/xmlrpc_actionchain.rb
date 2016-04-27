require "base64"
require_relative "xmlrpctest"

class XMLRPCActionChain < XMLRPCBaseTest
  def listChains()
    @connection.call("actionchain.listChains", @sid).map {|x| x['label']}
  end

  def createChain(label)
    @connection.call("actionchain.createChain", @sid, label)
  end


  def deleteChain(label)
    @connection.call("actionchain.deleteChain", @sid, label)
  end


  def removeAction(label, aid)
    @connection.call("actionchain.removeAction", @sid, label, aid)
  end


  def renameChain(oldLabel, newLabel)
    @connection.call("actionchain.renameChain", @sid, oldLabel, newLabel)
  end


  def addScriptRun(system, script, label)
    @connection.call("actionchain.addScriptRun",
                     @sid, system, label, "root", "root", 300,
                     Base64.encode64(script))
  end


  def listChainActions(label)
    @connection.call("actionchain.listChainActions", @sid, label)
  end


  def addSystemReboot(system, label)
    @connection.call("actionchain.addSystemReboot", @sid, system, label)
  end


  def addPackageInstall(system, packages, label)
    @connection.call("actionchain.addPackageInstall", @sid, system, packages, label)
  end


  def addPackageUpgrade(system, packages, label)
    @connection.call("actionchain.addPackageUpgrade", @sid, system, packages, label)
  end


  def addPackageVerify(system, packages, label)
    @connection.call("actionchain.addPackageVerify", @sid, system, packages, label)
  end


  def addPackageRemoval(system, packages, label)
    @connection.call("actionchain.addPackageRemoval", @sid, system, packages, label)
  end


  def scheduleChain(label, iso8601)
    @connection.call("actionchain.scheduleChain", @sid, label, iso8601)
  end
end
