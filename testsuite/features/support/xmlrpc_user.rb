# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# user class
class XMLRPCUserTest < XMLRPCBaseTest
  def get_users
    @connection.call('user.list_users', @sid)
  end

  def get_user_roles(uid)
    @connection.call('user.list_roles', @sid, uid)
  end

  def create_user(uid, password)
    @connection.call('user.create', @sid, uid, password, 'Thiel', 'Testerschmidt', 'thiel@test.suse.de')
  end

  def delete_user(uid)
    @connection.call('user.delete', @sid, uid)
  end

  def add_role(user, role)
    @connection.call('user.add_role', @sid, user, role)
  end

  def del_role(user, role)
    @connection.call('user.remove_role', @sid, user, role)
  end
end
