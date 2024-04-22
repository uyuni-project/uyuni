# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'pp'

# ct = CobblerTest.new
# bool = ct.running?
# bool = ct.element_exists('systems', 'bla')
# any  = ct.system_get_key('vbox-ug', 'uid')
# list = ct.get_list('systems')

# This class provides methods for interacting with the Cobbler server.
#
# The `CobblerTest` class is responsible for creating a new XMLRPC::client object and checking if the server is running.
# It also provides methods for logging in and out of Cobbler, creating distributions, profiles, and systems, modifying them,
# and performing other operations on the Cobbler server.
#
# Example usage:
#
# ```ruby
# cobbler = CobblerTest.new
# cobbler.login('admin', 'password')
# cobbler.distro_create('my_distro', '/path/to/kernel', '/path/to/initrd')
# cobbler.profile_create('my_profile', 'my_distro', '/path/to/kickstart')
# cobbler.system_create('my_system', 'my_profile')
# cobbler.logout
# ```
#
# For more information on the Cobbler API, refer to the official documentation: https://cobbler.readthedocs.io/
#
# @attr_reader [XMLRPC::Client] server The XMLRPC::Client object for communicating with the Cobbler server.
# @attr_reader [String] token The authentication token obtained after successful login.
class CobblerTest
  # Creates a new XMLRPC::client object, and then checks to see if the server is running.
  def initialize
    server_address = get_target('server').full_hostname
    @server = XMLRPC::Client.new2("http://#{server_address}/cobbler_api", nil, DEFAULT_TIMEOUT)
    raise(SystemCallError, "No running server at found at #{server_address}") unless running?
  end

  # Logs into Cobbler and returns the session token.
  #
  # @param user [String] The username for logging in to Cobbler.
  # @param pass [String] The password for logging in to Cobbler.
  # @return [String] The authentication token obtained after successful login.
  # @raise [StandardError] If the login to Cobbler fails.
  def login(user, pass)
    begin
      @token = @server.call('login', user, pass)
    rescue StandardError
      raise(StandardError, "Login to Cobbler failed. #{$ERROR_INFO}")
    end
  end

  # Logs out of Cobbler.
  #
  def logout
    begin
      @server.call('logout', @token)
    rescue StandardError
      raise(StandardError, "Logout to Cobbler failed. #{$ERROR_INFO}")
    end
  end

  # Returns true or false depending on whether the server is running.
  def running?
    result = true
    begin
      @server.call('get_profiles')
    rescue StandardError
      result = false
    end
    result
  end

  # Returns a list of the names of the systems, profiles, or distros in the database.
  #
  # @param what [String] The parameter indicating the type of items to retrieve.
  #   Valid values are 'systems', 'profiles', and 'distros'.
  # @return [Array<String>] An array containing the names of the retrieved items.
  # @raise [ArgumentError] If the specified parameter is not one of the valid values.
  def get_list(what)
    result = []
    raise(ArgumentError, "Unknown get_list parameter '#{what}'") unless %w[systems profiles distros].include?(what)

    ret = @server.call("get_#{what}")
    ret.each { |a| result << a['name'] }
    result
  end

  # Creates a new distribution with a specific name, kernel, initrd, and breed, and saves it.
  #
  # @param name [String] The name of the distribution.
  # @param kernel [String] The kernel file path for the distribution.
  # @param initrd [String] The initrd file path for the distribution.
  # @param breed [String] The breed of the distribution (default is 'suse').
  # @return [Integer] The ID of the created distribution.
  def distro_create(name, kernel, initrd, breed = 'suse')
    begin
      distro_id = @server.call('new_distro', @token)
      @server.call('modify_distro', distro_id, 'name', name, @token)
      @server.call('modify_distro', distro_id, 'kernel', kernel, @token)
      @server.call('modify_distro', distro_id, 'initrd', initrd, @token)
      @server.call('modify_distro', distro_id, 'breed', breed, @token)
      @server.call('save_distro', distro_id, @token)
    rescue StandardError
      raise(StandardError, "Creating distribution failed. #{$ERROR_INFO}")
    end
    distro_id
  end

  # Creates a new profile, modifies it, and saves it.
  #
  # @param name [String] The name of the profile.
  # @param distro [String] The name of the distribution you want to use.
  # @param location [String] The location of the kickstart file.
  # @return [Integer] The ID of the created profile.
  def profile_create(name, distro, location)
    begin
      profile_id = @server.call('new_profile', @token)
    rescue StandardError
      raise(StandardError, "Creating profile failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_profile', profile_id, 'name', name, @token)
      @server.call('modify_profile', profile_id, 'distro', distro, @token)
      @server.call('modify_profile', profile_id, 'kickstart', location, @token)
    rescue StandardError
      raise(StandardError, "Modifying profile failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_profile', profile_id, @token)
    rescue StandardError
      raise(StandardError, "Saving profile failed. #{$ERROR_INFO}")
    end
    profile_id
  end

  # Creates a system, sets its name and profile, and saves it.
  # Every system needs at least a name and a profile.
  #
  # @param name [String] The name of the system.
  # @param profile [String] The name of the profile to use for the system.
  # @return [Integer] The ID of the created system.
  def system_create(name, profile)
    begin
      system_id = @server.call('new_system', @token)
    rescue StandardError
      raise(StandardError, "Creating system failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_system', system_id, 'name', name, @token)
      @server.call('modify_system', system_id, 'profile', profile, @token)
    rescue StandardError
      raise(StandardError, "Modifying system failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_system', system_id, @token)
    rescue StandardError
      raise(StandardError, "Saving system failed. #{$ERROR_INFO}")
    end
    system_id
  end

  # Removes a system from the Spacewalk server.
  #
  # The first thing this function does is check to see if the system exists. If it doesn't, it raises an error. If it
  # does, it calls the remove_system function on the Spacewalk server. If that fails, it raises an error.
  #
  # @param name [String] The name of the system to be removed.
  def system_remove(name)
    raise(IndexError, "System cannot be found. #{$ERROR_INFO}") unless element_exists('systems', name)

    begin
      @server.call('remove_system', name, @token)
    rescue StandardError
      raise(StandardError, "Deleting system failed. #{$ERROR_INFO}")
    end
  end

  # Checks if a Cobbler item exists in the database by using
  # 'distros|profiles|systems|repos' as the table name,
  # 'name' as the column and the name of the item.
  #
  # @param name [String] The name of the distro.
  def element_exists(element_type, name)
    exists(element_type, 'name', name)
  end

  # Gets a key's value from a repository.
  #
  # @param name [String] The name of the repo
  # @param key [String] The key to get the value of
  #
  # @return [Object] The value of the key in the repo.
  def repo_get_key(name, key)
    return get('repo', name, key) if element_exists('repos', name)
    raise(IndexError, "Repo #{name} does not exist") unless element_exists('repos', name)
  end

  # Checks if a specific object with a certain key and value exists in the database.
  #
  # @param what [String] The name of the object you want to check for.
  # @param key [String] The key to check for.
  # @param value [Object] The value to check for.
  def exists(what, key, value)
    result = false
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = true if a[key] == value
    end
    result
  end

  # Retrieves an object from the server based on its type, name and key.
  #
  # @param what [String] The type of object you want to get.
  # @param name [String] The name of the object you want to get the ID of.
  # @param key [String] The key to look for in the hash.
  def get(what, name, key)
    result = nil
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = a[key] if a['name'] == name
    end
    result
  end

  # Modifies a profile and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-profile
  #
  # @param name [String] The name of the profile
  # @param attribute [String] The attribute you want to modify
  # @param value [Object] The new value you want to set for attribute
  def profile_modify(name, attribute, value)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: profile.uid
      profile = @server.call('get_profile_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Profile with name #{name} not found. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_profile', profile, attribute, value, @token)
    rescue ::StandardError
      raise(::StandardError, "Modifying profile failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_profile', profile, @token)
    rescue ::StandardError
      raise(::StandardError, "Saving profile failed. #{$ERROR_INFO}")
    end
    profile
  end

  # Modifies a distribution and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-distro
  #
  # @param name [String] The name of the distribution
  # @param attribute [String] The attribute you want to modify
  # @param value [Object] The new value you want to set for attribute
  def distro_modify(name, attribute, value)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: distro.uid
      distro = @server.call('get_distro_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Distribution with name #{name} not found. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_distro', distro, attribute, value, @token)
    rescue ::StandardError
      raise(::StandardError, "Modifying distribution failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_distro', distro, @token)
    rescue ::StandardError
      raise(::StandardError, "Saving distribution failed. #{$ERROR_INFO}")
    end
    distro
  end

  # Modifies a system and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-system
  #
  # @param name [String] The name of the system
  # @param attribute [String] The attribute you want to modify
  # @param value [Object] The new value you want to set for attribute
  def system_modify(name, attribute, value)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: system.uid
      system = @server.call('get_system_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "System with name #{name} not found. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_system', system, attribute, value, @token)
    rescue ::StandardError
      raise(::StandardError, "Modifying system failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_system', system, @token)
    rescue ::StandardError
      raise(::StandardError, "Saving system failed. #{$ERROR_INFO}")
    end
    system
  end

  # Removes a distribution from the Spacewalk server.
  #
  # The first thing this function does is check to see if the distribution exists. If it doesn't, it raises an error.
  # If it does, it calls the remove_distro function on the Spacewalk server. If that fails, it raises an error.
  #
  # @param name [String] The name of the distribution to be removed.
  def distro_remove(name)
    raise(::IndexError, "Distribution cannot be found. #{$ERROR_INFO}") unless distro_exists(name)

    begin
      @server.call('remove_distro', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Deleting distribution failed. #{$ERROR_INFO}")
    end
  end

  # Removes a profile from the Spacewalk server.
  #
  # The first thing this function does is check to see if the profile exists. If it doesn't, it raises an error. If it
  # does, it calls the remove_profile function on the Spacewalk server. If that fails, it raises an error.
  #
  # @param name [String] The name of the profile to be removed.
  def profile_remove(name)
    raise(::IndexError, "Profile cannot be found. #{$ERROR_INFO}") unless profile_exists(name)

    begin
      @server.call('remove_profile', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Deleting profile failed. #{$ERROR_INFO}")
    end
  end

  # Get a handle for a system.
  #
  # Get a handle for a system which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # @param name [String] The name of the system to get the ID of
  def get_system_handle(name)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: system.uid
      system = @server.call('get_system_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "System with name #{name} not found. #{$ERROR_INFO}")
    end
    system
  end

  # Get a handle for a profile.
  #
  # Get a handle for a profile which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # @param name [String] The name of the profile to get the ID of
  def get_profile_handle(name)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: profile.uid
      system = @server.call('get_profile_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Profile with name #{name} not found. #{$ERROR_INFO}")
    end
    system
  end

  # Get a handle for a distribution.
  #
  # Get a handle for a distribution which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # @param name [String] The name of the distribution to get the ID of
  def get_distro_handle(name)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: distro.uid
      system = @server.call('get_distro_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Distribution with name #{name} not found. #{$ERROR_INFO}")
    end
    system
  end
end
