#!/usr/ruby

require 'xmlrpc/client'

class XMLRPCBaseTest
  #
  # Constructor.
  #
  def initialize(host)
    @host = host
    if !@host
      raise Exception, "Hostname is missing!"
    end
    @connection = XMLRPC::Client.new2("http://" + @host + "/rpc/api")
  end


  #
  # Authenticate against the $HOST
  #
  def login(luser, password)
    begin
      @sid = @connection.call("auth.login", luser, password)
      return true
    rescue Exception => exception
      puts "Login failed. Try harder. :)"
      return false
    end
  end


  def logout()
    begin
      @connection.call("auth.logout", @sid)
      return true
    rescue Exception => exception
      puts "Well, you finished anyways..."
      return false
    end
  end
end

