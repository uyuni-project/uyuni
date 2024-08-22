# Copyright (c) 2022-2024 SUSE LLC.
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
require 'date'

# Abstract parent class describing an API test
class ApiTest
  # Creates objects that are used to interact with the API.
  #
  # @param _host [String] The hostname of the Spacewalk server.
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

  # Calls a function with the given name and parameters, and returns its response.
  #
  # @param name [String] The name of the method you want to call.
  # @param params [Array] The parameters to pass to the API call.
  # @return [Object] The response from the API call.
  def call(name, *params)
    thread =
      Thread.new do
        @semaphore.synchronize do
          begin
            manage_api_lock(name)
            response = make_api_call(name, *params)
          ensure
            @connection.call('auth.logout', sessionKey: @token) if @token
            api_unlock if name.include?('user.')
          end
          response
        end
      end
    thread.value
  end

  private

  # Handles API lock management
  def manage_api_lock(name)
    if name.include?('user.')
      repeat_until_timeout(timeout: DEFAULT_TIMEOUT, message: 'We couldn\'t get access to the API') do
        break unless api_lock?

        sleep 1
      end
      @token = @connection.call('auth.login', login: 'admin', password: 'admin')
    else
      @token = @connection.call('auth.login', login: $current_user, password: $current_password)
    end
  end

  # Makes the actual API call
  def make_api_call(name, *params)
    params[0][:sessionKey] = @token
    @connection.call(name, *params)
  end
end

# Derived class for an XML-RPC test
class ApiTestXmlrpc < ApiTest
  # Creates a new instance of the XmlrpcClient class, and assigns it to the @connection instance variable.
  #
  # @param host [String] The hostname of the server.
  def initialize(host)
    super
    @connection = XmlrpcClient.new(host)
  end

  # Returns a boolean on whether the given attribute is an XMLRPC::DateTime object or not
  #
  # @param attribute [Object] The attribute to check.
  # @return [Boolean] Whether the attribute is an XMLRPC::DateTime object or not.
  def date?(attribute)
    attribute.instance_of?(XMLRPC::DateTime)
  end

  # Returns the current date and time as an XMLRPC::DateTime object.
  #
  # @return [XMLRPC::DateTime] The current date and time as an XMLRPC::DateTime object.
  def date_now
    now = Time.now
    XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  end
end

# Derived class for an HTTP test
class ApiTestHttp < ApiTest
  # It creates a new instance of the HttpClient class.
  #
  # @param host [String] The hostname of the server.
  # @param ssl_verify [Boolean] Whether to verify SSL certificates or not.
  def initialize(host, ssl_verify = true)
    super(host)
    @connection = HttpClient.new(host, ssl_verify)
  end

  # Attempts to parse a given string as a Date object, to validate it.
  # Returns a boolean on whether it's a string containing a valid Date or not.
  #
  # @param attribute [String] The date object to be parsed.
  # @return [Boolean] Whether the attribute is a valid Date or not.
  def date?(attribute)
    begin
      Date.parse(attribute)
      true
    rescue ArgumentError
      false
    end
  end

  # It returns a string with the current date and time in the format `YYYY-MM-DDTHH:MM:SS.LLL+HHMM`
  #
  # @return [String] The current date and time in the specified format.
  def date_now
    now = Time.now
    now.strftime('%Y-%m-%dT%H:%M:%S.%L%z')
  end
end
