# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'namespaces/actionchain'
require_relative 'namespaces/activationkey'
require_relative 'namespaces/api'
require_relative 'namespaces/audit'
require_relative 'namespaces/channel'
require_relative 'namespaces/configchannel'
require_relative 'namespaces/image'
require_relative 'namespaces/kickstart'
require_relative 'namespaces/schedule'
require_relative 'namespaces/system'
require_relative 'namespaces/user'
require_relative 'xmlrpc_client'
require_relative 'http_client'

# Abstract parent class describing an API test test
class ApiTest
  ##
  # Creates objects that are used to interact with the API.
  #
  # Args:
  #   _host: The hostname of the Spacewalk server.
  def initialize(_host)
    @actionchain = NamespaceActionchain.new(self)
    @activationkey = NamespaceActivationkey.new(self)
    @api = NamespaceApi.new(self)
    @audit = NamespaceAudit.new(self)
    @channel = NamespaceChannel.new(self)
    @configchannel = NamespaceConfigchannel.new(self)
    @image = NamespaceImage.new(self)
    @kickstart = NamespaceKickstart.new(self)
    @schedule = NamespaceSchedule.new(self)
    @system = NamespaceSystem.new(self)
    @user = NamespaceUser.new(self)
    @connection = nil
    @token = nil
    @semaphore = Mutex.new
  end

  attr_reader :actionchain
  attr_reader :activationkey
  attr_reader :api
  attr_reader :audit
  attr_reader :channel
  attr_reader :configchannel
  attr_reader :image
  attr_reader :kickstart
  attr_reader :schedule
  attr_reader :system
  attr_reader :user
  attr_accessor :token

  ##
  # Calls a function with the given name and parameters, and returns its response.
  #
  # Args:
  #   name: The name of the method you want to call.
  #   *params: The parameters to pass to the API call.
  def call(name, *params)
    thread =
      Thread.new do
        @semaphore.synchronize do
          @token = @connection.call('auth.login', login: 'admin', password: 'admin')
          params[0][:sessionKey] = @token
          response = @connection.call(name, *params)
          @connection.call('auth.logout', sessionKey: @token)
          response
        end
      end
    thread.value
  end
end

# Derived class for an XML-RPC test
class ApiTestXmlrpc < ApiTest
  ##
  # Creates a new instance of the XmlrpcClient class, and assigns it to the @connection instance variable.
  #
  # Args:
  #   host: The hostname of the server.
  def initialize(host)
    super
    @connection = XmlrpcClient.new(host)
  end

  ##
  # Returns a boolean on whether the given attribute is an XMLRPC::DateTime object or not
  def date?(attribute)
    attribute.instance_of?(XMLRPC::DateTime)
  end

  ##
  # Returns the current date and time as an XMLRPC::DateTime object.
  def date_now
    now = Time.now
    XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  end
end

# Derived class for an HTTP test
class ApiTestHttp < ApiTest
  ##
  # It creates a new instance of the HttpClient class.
  #
  # Args:
  #   host: The hostname of the server.
  def initialize(host, ssl_verify = true)
    super(host)
    @connection = HttpClient.new(host, ssl_verify)
  end

  ##
  # Attempts to parse a given string as a Date object, to validate it.
  # Returns a boolean on whether it's a string containing a valid Date or not.
  #
  # Args:
  #     attribute: The date object to be parsed.
  def date?(attribute)
    begin
      ok = true
      Date.parse(attribute)
    rescue ArgumentError
      ok = false
    end
    ok
  end

  ##
  # It returns a string with the current date and time in the format `YYYY-MM-DDTHH:MM:SS.LLL+HHMM`
  def date_now
    now = Time.now
    now.strftime('%Y-%m-%dT%H:%M:%S.%L%z')
  end
end
