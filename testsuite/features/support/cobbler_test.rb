# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'pp'

# ct = CobblerTest.new
# bool = ct.running?
# bool = ct.element_exists('systems', 'bla')
# any  = ct.system_get_key('vbox-ug', 'uid')
# list = ct.get_list('systems')

# Class for clobber test
class CobblerTest
  ##
  # Creates a new XMLRPC::client object, and then checks to see if the server is running.
  def initialize
    server_address = ENV['SERVER']
    @server = XMLRPC::Client.new2("http://#{server_address}/cobbler_api", nil, DEFAULT_TIMEOUT)
    raise(SystemCallError, "No running server at found at #{server_address}") unless running?
  end

  ##
  # Logs into Cobbler and returns the session token.
  #
  # Args:
  #   user: The username used to log in.
  #   pass: The password for the user.
  def login(user, pass)
    @token = @server.call('login', user, pass)
  rescue StandardError
    raise(StandardError, "Login to Cobbler failed. #{$ERROR_INFO}")
  end

  ##
  # Logs out of Cobbler.
  #
  def logout
    @server.call('logout', @token)
  rescue StandardError
    raise(StandardError, "Logout to Cobbler failed. #{$ERROR_INFO}")
  end

  ##
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

  ##
  # Returns a list of the names of the systems, profiles, or distros in the database.
  #
  # Args:
  #   what: The type of list you want to get.  Valid values are:
  #        - systems
  #        - profiles
  #        - distros
  def get_list(what)
    result = []
    raise(ArgumentError, "Unknown get_list parameter '#{what}'") unless %w[systems profiles distros].include?(what)

    ret = @server.call("get_#{what}")
    ret.each { |a| result << a['name'] }
    result
  end

  ##
  # Creates a new distribution with a specific name, kernel, initrd, and breed, and saves it.
  #
  # Args:
  #   name: The name of the distribution.
  #   kernel: The path to the kernel file.
  #   initrd: The initrd file for the distribution.
  #   breed: The type of distribution.  This can be one of the following: redhat, debian, suse. Defaults to suse.
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

  ##
  # Creates a new profile, modifies it, and saves it.
  #
  # Args:
  #   name: The name of the profile.
  #   distro: The name of the distribution you want to use.
  #   location: The location of the kickstart file.
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

  ##
  # Creates a system, sets its name and profile, and saves it.
  # Every system needs at least a name and a profile.
  #
  # Args:
  #   name: The name of the system.
  #   profile: The name of the profile to use for the system.
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

  ##
  # Removes a system from the Spacewalk server.
  #
  # The first thing this function does is check to see if the system exists. If it doesn't, it raises an error. If it
  # does, it calls the remove_system function on the Spacewalk server. If that fails, it raises an error.
  #
  # Args:
  #   name: The name of the system to be removed.
  def system_remove(name)
    raise(IndexError, "System cannot be found. #{$ERROR_INFO}") unless element_exists('systems', name)

    begin
      @server.call('remove_system', name, @token)
    rescue StandardError
      raise(StandardError, "Deleting system failed. #{$ERROR_INFO}")
    end
  end

  ##
  # Checks if a distribution exists in the database by using 'distro' as the table name, 'name' as the column and the name of the distro.
  # Checks if a Cobbler item exists in the database by using
  # 'distros|profiles|systems|repos' as the table name,
  # 'name' as the column and the name of the item.
  #
  # Args:
  #   element_type: The type of the element
  #   name: The name of the element.
  def element_exists(element_type, name)
    exists(element_type, 'name', name)
  end

  ##
  # Gets a key's value from a repository.
  #
  # Args:
  #   name: The name of the repo
  #   key: The key to get the value of
  #
  # Returns:
  #   The value of the key in the repo.
  def repo_get_key(name, key)
    return get('repo', name, key) if element_exists('repos', name)
    raise(IndexError, "Repo #{name} does not exist") unless element_exists('repos', name)
  end

  ##
  # Checks if a specific object with a certain key and value exists in the database.
  #
  # Args:
  #   what: The name of the object you want to check for.
  #   key: The key to check for.
  #   value: The value to check for.
  def exists(what, key, value)
    result = false
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = true if a[key] == value
    end
    result
  end

  ##
  # Retrieves an object from the server based on its type, name and key.
  #
  # Args:
  #   what: The type of object you want to get. This can be one of the following: # TODO
  #   name: The name of the object you want to get the ID of.
  #   key: The key to look for in the hash.
  def get(what, name, key)
    result = nil
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = a[key] if a['name'] == name
    end
    result
  end

  ##
  # Modifies a profile and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-profile
  # Args:
  #   name: The name of the profile
  #   attribute: The attribute you want to modify
  #   value: The new value you want to set for attribute
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

  ##
  # Modifies a distribution and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-distro
  # Args:
  #   name: The name of the distribution
  #   attribute: The attribute you want to modify
  #   value: The new value you want to set for attribute
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

  ##
  # Modifies a system and saves it afterwards.
  #
  # For more information, see https://cobbler.readthedocs.io/en/latest/cobbler.html#cobbler-system
  # Args:
  #   name: The name of the system
  #   attribute: The attribute you want to modify
  #   value: The new value you want to set for attribute
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

  ##
  # Removes a distribution from the Spacewalk server.
  #
  # The first thing this function does is check to see if the distribution exists. If it doesn't, it raises an error.
  # If it does, it calls the remove_distro function on the Spacewalk server. If that fails, it raises an error.
  #
  # Args:
  #   name: The name of the distribution to be removed.
  def distro_remove(name)
    raise(::IndexError, "Distribution cannot be found. #{$ERROR_INFO}") unless element_exists('distros', name)

    begin
      @server.call('remove_distro', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Deleting distribution failed. #{$ERROR_INFO}")
    end
  end

  ##
  # Removes a profile from the Spacewalk server.
  #
  # The first thing this function does is check to see if the profile exists. If it doesn't, it raises an error. If it
  # does, it calls the remove_profile function on the Spacewalk server. If that fails, it raises an error.
  #
  # Args:
  #   name: The name of the profile to be removed.
  def profile_remove(name)
    raise(::IndexError, "Profile cannot be found. #{$ERROR_INFO}") unless element_exists('profiles', name)

    begin
      @server.call('remove_profile', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Deleting profile failed. #{$ERROR_INFO}")
    end
  end

  ##
  # Get a handle for a system.
  #
  # Get a handle for a system which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # Args:
  #   name: The name of the system to get the ID of
  def get_system_handle(name)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: system.uid
      system = @server.call('get_system_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "System with name #{name} not found. #{$ERROR_INFO}")
    end
    system
  end

  ##
  # Get a handle for a profile.
  #
  # Get a handle for a profile which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # Args:
  #   name: The name of the profile to get the ID of
  def get_profile_handle(name)
    begin
      # TODO: Starting with Cobbler 3.4.0 the handle will be the UID: profile.uid
      system = @server.call('get_profile_handle', name, @token)
    rescue ::StandardError
      raise(::StandardError, "Profile with name #{name} not found. #{$ERROR_INFO}")
    end
    system
  end

  ##
  # Get a handle for a distribution.
  #
  # Get a handle for a distribution which allows you to use the functions modify_* or save_* to manipulate it.
  #
  # Args:
  #   name: The name of the distribution to get the ID of
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
