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
  def auth(luser, password)
    begin
      @sid = @connection.call("auth.login", luser, password)
      puts @sid
    rescue Exception => exception
      puts "Login failed. Try harder. :)"
    end
  end

  def logout()
    begin
      @connection.call("auth.logout", @sid)
    rescue Exception => exception
      puts "Well, you finished anyways..."
    end
  end
end


# Local execution only
if __FILE__ == $0
  test = XMLRPCApiTest.new("hoag.suse.de")
  if test.getVersion("10.15")
    puts "yeeaaa!!!"
  else
    puts ":-("
  end
end

