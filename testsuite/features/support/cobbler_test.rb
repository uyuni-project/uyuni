# Copyright (c) 2010-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'pp'

# ct = CobblerTest.new( "taylor.suse.de" )
# bool = ct.running?()
# bool = ct.system_exists( "bla" )
# any  = ct.system_get_key( "vbox-ug", "uid" )
# list = ct.get_list( "systems" )

# Class for clobber test
class CobblerTest
  # Initialize the class instantiating a XMLRPC client
  def initialize(server_address = ENV['SERVER'], server_port = 80, server_path = '/cobbler_api')
    @server_address = server_address
    @server_port = server_port
    @server_path = server_path
    @server = XMLRPC::Client.new(server_address, server_path, server_port)
    raise(SystemCallError, "No running server at found at #{server_address}") unless running?
  end

  # Log-in using user and password to the XMLRPC Server API
  def login(user, pass)
    @token = @server.call('login', user, pass)
  rescue StandardError
    raise(StandardError, "Login to cobbler failed. #{$ERROR_INFO}")
  end

  # Check if the server is running
  def running?
    result = true
    begin
      @server.call('get_profiles')
    rescue StandardError
      result = false
    end
    result
  end

  # Returns a list of a specified attribute
  def get_list(what)
    result = []
    raise(ArgumentError, "unknown get_list parameter '#{what}'") unless %w[systems profiles distros].include?(what)

    ret = @server.call("get_#{what}")
    ret.each { |a| result << a['name'] }
    result
  end

  # Create a Cobbler profile
  def profile_create(name, distro, location)
    begin
      profile_id = @server.call('new_profile', @token)
    rescue StandardError
      raise(StandardError, "creating profile failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('modify_profile', profile_id, 'name', name, @token)
      @server.call('modify_profile', profile_id, 'distro', distro, @token)
      @server.call('modify_profile', profile_id, 'kickstart', location, @token)
    rescue StandardError
      raise(StandardError, "modify profile failed. #{$ERROR_INFO}")
    end
    begin
      @server.call('save_profile', profile_id, @token)
    rescue StandardError
      raise(StandardError, "saving profile failed. #{$ERROR_INFO}")
    end
    profile_id
  end

  # Checks if the Cobbler profile exist
  def profile_exists(name)
    exists('profiles', 'name', name)
  end

  # Checks if the system exist
  def system_exists(name)
    exists('systems', 'name', name)
  end

  # Checks if the distribution exist
  def distro_exists(name)
    exists('distros', 'name', name)
  end

  # Create a distribution
  def distro_create(name, kernel, initrd, breed = 'suse')
    begin
      distro_id = @server.call('new_distro', @token)
      @server.call('modify_distro', distro_id, 'name', name, @token)
      @server.call('modify_distro', distro_id, 'kernel', kernel, @token)
      @server.call('modify_distro', distro_id, 'initrd', initrd, @token)
      @server.call('modify_distro', distro_id, 'breed', breed, @token)
      @server.call('save_distro', distro_id, @token)
    rescue StandardError
      raise(StandardError, "creating distribution failed. #{$ERROR_INFO}")
    end
    distro_id
  end

  # Check if the repository exist
  def repo_exists(name)
    exists('repos', 'name', name)
  end

  # Returns the key of a repository
  def repo_get_key(name, key)
    return get('repo', name, key) if repo_exists(name)
    raise(IndexError, "Repo #{name} does not exists") unless repo_exists(name)
  end

  # Check if a specified attribute with a key,value exist
  def exists(what, key, value)
    result = false
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = true if a[key] == value
    end
    result
  end

  # Returns a specified attribute, by name,key
  def get(what, name, key)
    result = nil
    ret = @server.call("get_#{what}")
    ret.each do |a|
      result = a[key] if a['name'] == name
    end
    result
  end
end
