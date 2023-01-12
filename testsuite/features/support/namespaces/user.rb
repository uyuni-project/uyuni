# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "user" namespace
class NamespaceUser
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # `list_users` is a function that calls the `user.listUsers` method of the `@test` object, passing in the `sessionKey`
  # as the `@test.token` value
  def list_users
    @test.call('user.listUsers', sessionKey: @test.token)
  end

  ##
  # > This function lists the roles of a user
  #
  # Args:
  #   user: The user you want to list the roles of.
  def list_roles(user)
    @test.call('user.listRoles', sessionKey: @test.token, login: user)
  end

  ##
  # This function creates a user with the given parameters
  #
  # Args:
  #   user: The username of the user you want to create.
  #   password: password
  #   first: First name of the user
  #   last: last name of the user
  #   email: email address of the user
  def create(user, password, first, last, email)
    @test.call('user.create', sessionKey: @test.token, login: user, password: password, firstName: first, lastName: last, email: email)
  end

  ##
  # This function deletes a user from the system
  #
  # Args:
  #   user: The username of the user you want to delete.
  def delete(user)
    @test.call('user.delete', sessionKey: @test.token, login: user)
  end

  ##
  # This function adds a role to a user
  #
  # Args:
  #   user: The user's login name
  #   role: The role to add to the user.
  def add_role(user, role)
    @test.call('user.addRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # This function removes a role from a user
  #
  # Args:
  #   user: The user's login name
  #   role: The role to add to the user.
  def remove_role(user, role)
    @test.call('user.removeRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # It takes a user's login name and returns their details
  #
  # Args:
  #   user: The user's login name
  def get_details(user)
    @test.call('user.getDetails', sessionKey: @test.token, login: user)
  end
end
