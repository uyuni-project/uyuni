# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# User namespace
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
  # List all users
  def list_users
    @test.call('user.listUsers', sessionKey: @test.token)
  end

  ##
  # Lists the roles of a user.
  #
  # Args:
  #   user: The user you want to list the roles of.
  def list_roles(user)
    @test.call('user.listRoles', sessionKey: @test.token, login: user)
  end

  ##
  # Creates a user with the given parameters.
  #
  # Args:
  #   user: The username of the user you want to create.
  #   password: password.
  #   first: First name of the user.
  #   last: last name of the user.
  #   email: email address of the user.
  def create(user, password, first, last, email)
    @test.call('user.create', sessionKey: @test.token, login: user, password: password, firstName: first, lastName: last, email: email)
  end

  ##
  # Deletes a user from the system.
  #
  # Args:
  #   user: The username of the user you want to delete.
  def delete(user)
    @test.call('user.delete', sessionKey: @test.token, login: user)
  end

  ##
  # Adds a role to a user.
  #
  # Args:
  #   user: The user's login name.
  #   role: The role to add to the user.
  def add_role(user, role)
    @test.call('user.addRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # Removes a role from a user.
  #
  # Args:
  #   user: The user's login name.
  #   role: The role to add to the user.
  def remove_role(user, role)
    @test.call('user.removeRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # It takes a user's login name and returns their details.
  #
  # Args:
  #   user: The user's login name.
  def get_details(user)
    @test.call('user.getDetails', sessionKey: @test.token, login: user)
  end
end
