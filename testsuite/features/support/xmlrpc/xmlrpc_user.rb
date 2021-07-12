# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# User API Namespace
class XMLRPCUserTest < XMLRPCBaseTest
  # Get Users
  def users
    @connection.call('user.list_users', @sid)
  end

  # Get User roles
  def user_roles(uid)
    @connection.call('user.list_roles', @sid, uid)
  end

  # Create a user
  def create_user(uid, password)
    @connection.call('user.create', @sid, uid, password, 'Thiel', 'Testerschmidt', 'thiel@test.suse.de')
  end

  # Delete a user by UID
  def delete_user(uid)
    @connection.call('user.delete', @sid, uid)
  end

  # Add a role into a user
  def add_role(user, role)
    @connection.call('user.add_role', @sid, user, role)
  end

  # Delete a role into a user
  def del_role(user, role)
    @connection.call('user.remove_role', @sid, user, role)
  end
end
