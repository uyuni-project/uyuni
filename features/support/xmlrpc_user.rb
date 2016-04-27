require_relative 'xmlrpctest'

class XMLRPCUserTest < XMLRPCBaseTest

  def getUsers
    @connection.call("user.listUsers", @sid)
  end

  def getUserRoles(uid)
    @connection.call("user.listRoles", @sid, uid)
  end

  def createUser(uid, password)
    @connection.call("user.create", @sid, uid, password, "Thiel", "Testerschmidt", "thiel@test.suse.de")
  end

  def deleteUser(uid)
    @connection.call("user.delete", @sid, uid)
  end

  def addRole(user, role)
    @connection.call("user.addRole", @sid, user, role)
  end

  def delRole(user, role)
    @connection.call("user.removeRole", @sid, user, role)
  end
end
