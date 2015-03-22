# COPYRIGHT 2015 SUSE LLC

File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCUserTest < XMLRPCBaseTest
  def getUserIds()
    users = @connection.call("user.listUsers", @sid)
    ids = []
    for user in users
      ids.push(user['login'])
    end
  end


  def getUserRoles(uid)
    return @connection.call("user.listRoles", @sid, uid)
  end


  def createUser(uid, password)
    return @connection.call("user.create", @sid, uid, password, "Thiel", "Testerschmidt", "thiel@test.suse.de")
  end


  def deleteUser(uid)
    return @connection.call("user.delete", @sid, uid)
  end


  def addRole(user, role)
    return @connection.call("user.addRole", @sid, user, role)
  end


  def delRole(user, role)
    return @connection.call("user.removeRole", @sid, user, role)
  end
end
