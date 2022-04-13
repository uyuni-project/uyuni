# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "user" namespace
class NamespaceUser
  def initialize(api_test)
    @test = api_test
  end

  def list_users
    @test.call('user.listUsers', sessionKey: @test.token)
  end

  def list_roles(user)
    @test.call('user.listRoles', sessionKey: @test.token, login: user)
  end

  def create(user, password, first, last, email)
    @test.call('user.create', sessionKey: @test.token, desiredLogin: user, desiredPassword: password, firstName: first, lastName: last, email: email)
  end

  def delete(user)
    @test.call('user.delete', sessionKey: @test.token, login: user)
  end

  def add_role(user, role)
    @test.call('user.addRole', sessionKey: @test.token, login: user, role: role)
  end

  def remove_role(user, role)
    @test.call('user.removeRole', sessionKey: @test.token, login: user, role: role)
  end

  def get_details(user)
    @test.call('user.getDetails', sessionKey: @test.token, login: user)
  end
end
