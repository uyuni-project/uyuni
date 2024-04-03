# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

##
# This class represents a user in the namespace.
class NamespaceUser
  ##
  # Initializes a new instance of the NamespaceUser class.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Lists all users.
  #
  # @return [Object] The result of the API call.
  def list_users
    @test.call('user.listUsers', sessionKey: @test.token)
  end

  ##
  # Lists the roles of a user.
  #
  # @param user [String] The username of the user.
  # @return [Object] The result of the API call.
  def list_roles(user)
    @test.call('user.listRoles', sessionKey: @test.token, login: user)
  end

  ##
  # Creates a user with the given parameters.
  #
  # @param user [String] The username of the user to create.
  # @param password [String] The password of the user.
  # @param first [String] The first name of the user.
  # @param last [String] The last name of the user.
  # @param email [String] The email address of the user.
  # @return [Object] The result of the API call.
  def create(user, password, first, last, email)
    @test.call('user.create', sessionKey: @test.token, login: user, password: password, firstName: first, lastName: last, email: email)
  end

  ##
  # Deletes a user from the system.
  #
  # @param user [String] The username of the user to delete.
  # @return [Object] The result of the API call.
  def delete(user)
    @test.call('user.delete', sessionKey: @test.token, login: user)
  end

  ##
  # Adds a role to a user.
  #
  # @param user [String] The user's login name.
  # @param role [String] The role to add to the user.
  # @return [Object] The result of the API call.
  def add_role(user, role)
    @test.call('user.addRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # Removes a role from a user.
  #
  # @param user [String] The user's login name.
  # @param role [String] The role to remove from the user.
  # @return [Object] The result of the API call.
  def remove_role(user, role)
    @test.call('user.removeRole', sessionKey: @test.token, login: user, role: role)
  end

  ##
  # Gets the details of a user.
  #
  # @param user [String] The user's login name.
  # @return [Object] The result of the API call.
  def get_details(user)
    @test.call('user.getDetails', sessionKey: @test.token, login: user)
  end
end
