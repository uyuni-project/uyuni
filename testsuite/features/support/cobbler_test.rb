# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'pp'

# ct = CobblerTest.new
# bool = ct.running?
# bool = ct.system_exists('bla')
# any  = ct.system_get_key('vbox-ug', 'uid')
# list = ct.get_list('systems')

# Class for clobber test
class CobblerTest
  ##
  # Creates a new XMLRPC::client object, and then checks to see if the server is running.
  def initialize
    server_address = ENV['SERVER']
    @server = XMLRPC::Client.new2('http://' + server_address + '/cobbler_api', nil, DEFAULT_TIMEOUT)
    raise 'No running server at found at ' + server_address unless running?
  end

  ##
  # Logs into Cobbler and returns the session token.
  #
  # Args:
  #   user: The username used to log in.
  #   pass: The password for the user.
  def login(user, pass)
    @token = @server.call('login', user, pass)
  rescue
    raise 'login to cobbler failed' + $ERROR_INFO.to_s
  end

  ##
  # Returns true or false depending on whether the server is running.
  def running?
    result = true
    begin
      @server.call('get_profiles')
    rescue
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
    unless %w[systems profiles distros].include?(what)
      raise "unknown get_list parameter '#{what}'"
    end
    ret = @server.call('get_' + what)
    ret.each { |a| result << a['name'] }
    result
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
    rescue
      raise 'creating profile failed.' + $ERROR_INFO.to_s
    end
    begin
      @server.call('modify_profile', profile_id, 'name', name, @token)
      @server.call('modify_profile', profile_id, 'distro', distro, @token)
      @server.call('modify_profile', profile_id, 'kickstart', location, @token)
    rescue
      raise 'modify profile failed.' + $ERROR_INFO.to_s
    end
    begin
      @server.call('save_profile', profile_id, @token)
    rescue
      raise 'saving profile failed.' + $ERROR_INFO.to_s
    end
    profile_id
  end

  ##
  # Checks if a profile exists in the database by using 'profiles' as the table name, 'name' as the column and the name of the distro.
  #
  # Args:
  #   name: The name of the profile.
  def profile_exists(name)
    exists('profiles', 'name', name)
  end

  ##
  # Checks if a system exists in the database by using 'systems' as the table name, 'name' as the column and the name of the distro.
  #
  # Args:
  #   name: The name of the system.
  def system_exists(name)
    exists('systems', 'name', name)
  end

  ##
  # Checks if a distribution exists in the database by using 'distros' as the table name, 'name' as the column and the name of the distro.
  #
  # Args:
  #   name: The name of the distro.
  def distro_exists(name)
    exists('distros', 'name', name)
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
    rescue
      raise 'creating distribution failed.' + $ERROR_INFO.to_s
    end
    distro_id
  end

  ##
  # Checks if a repository exists in the database by using 'repos' as the table name, 'name' as the column and the name of the repo.
  #
  # Args:
  #   name: The name of the repository.
  def repo_exists(name)
    exists('repos', 'name', name)
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
    return get('repo', name, key) if repo_exists(name)
    raise 'Repo ' + name + ' does not exists' unless repo_exists(name)
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
    ret = @server.call('get_' + what)
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
    ret = @server.call('get_' + what)
    ret.each do |a|
      result = a[key] if a['name'] == name
    end
    result
  end
end
