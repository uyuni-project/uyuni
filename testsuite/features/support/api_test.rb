# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'namespaces/actionchain'
require_relative 'namespaces/activationkey'
require_relative 'namespaces/api'
require_relative 'namespaces/audit'
require_relative 'namespaces/auth'
require_relative 'namespaces/channel'
require_relative 'namespaces/configchannel'
require_relative 'namespaces/image'
require_relative 'namespaces/kickstart'
require_relative 'namespaces/schedule'
require_relative 'namespaces/system'
require_relative 'namespaces/user'
require_relative 'xmlrpc_client'
require_relative 'http_client'

# Abstract parent class describing an API test
class ApiTest
  def initialize(_host)
    @actionchain = NamespaceActionchain.new(self)
    @activationkey = NamespaceActivationkey.new(self)
    @api = NamespaceApi.new(self)
    @audit = NamespaceAudit.new(self)
    @auth = NamespaceAuth.new(self)
    @channel = NamespaceChannel.new(self)
    @configchannel = NamespaceConfigchannel.new(self)
    @image = NamespaceImage.new(self)
    @kickstart = NamespaceKickstart.new(self)
    @schedule = NamespaceSchedule.new(self)
    @system = NamespaceSystem.new(self)
    @user = NamespaceUser.new(self)
    @connection = nil
    @token = nil
  end

  attr_reader :actionchain
  attr_reader :activationkey
  attr_reader :api
  attr_reader :audit
  attr_reader :auth
  attr_reader :channel
  attr_reader :configchannel
  attr_reader :image
  attr_reader :kickstart
  attr_reader :schedule
  attr_reader :system
  attr_reader :user
  attr_reader :token
  attr_writer :token

  def call(*params)
    @connection.call(*params)
  end
end

# Derived class for an XML-RPC test
class ApiTestXmlrpc < ApiTest
  def initialize(host)
    super
    @connection = XmlrpcClient.new(host)
  end

  # during XML-RPC tests, dates are XMLRPC::DateTime's
  def date?(attribute)
    attribute.class == XMLRPC::DateTime
  end

  def date_now
    now = Time.now
    XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  end
end

# Derived class for an HTTP test
class ApiTestHttp < ApiTest
  def initialize(host)
    super
    @connection = HttpClient.new(host)
  end

  # during HTTP tests, dates are strings
  def date?(attribute)
    begin
      ok = true
      Date.parse(attribute)
    rescue ArgumentError
      ok = false
    end
    ok
  end

  def date_now
    now = Time.now
    now.strftime('%Y-%m-%dT%H:%M:%S.%L%z')
  end
end
