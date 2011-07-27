#
# Test classes
#
class XMLRPCApiTest < XMLRPCBaseTest
  def getVersion()
    return @connection.call("api.getVersion")
  end


  def systemVersion()
    return @connection.call("api.systemVersion")
  end


  def getCountOfApiNamespaces()
    namespaces = @connection.call("api.getApiNamespaces", @sid)
    count = 0
    if namespaces != nil
      count = namespaces.length
    end

    return count
  end


  #
  # Test lists all available api calls grouped by namespace.
  #
  def getCountOfApiCallListGroups()
    callList = @connection.call("api.getApiCallList", @sid)
    count = 0
    if callList != nil
      count = callList.length
    end

    return count
  end


  def getCountOfApiNamespaceCallList()
    count = 0
    namespaces = @connection.call("api.getApiNamespaces", @sid)
    puts "    Spaces found: " + namespaces.length.to_s
    for ns in namespaces
      print "      Analyzing " + ns[0] + "... "
      callList = @connection.call("api.getApiNamespaceCallList", @sid, ns[0])
      if callList != nil
        count += callList.length
        puts "Done"
      else
        puts "Failed"
      end
    end

    return count
  end
end

rpcApiTester = XMLRPCApiTest.new(ENV["TESTHOST"])


#
# Steps
#
Given /^I am logged in via XML\-RPC as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  fail if not rpcApiTester.login(luser, password)
end

When /^I call getApiNamespaces, I should get (\d+) namespaces$/ do |apiCount|
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaces()
end

When /^I call getVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.getVersion() == version
end

When /^I call systemVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.systemVersion() == version
end

When /^I call getApiNamespaceCallList, I should get (\d+) available api calls$/ do |apiCount|
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaceCallList()
end

When /^I call getApiCallList, I should get (\d+) available groups$/ do |groupCount|
  fail if groupCount.to_i != rpcApiTester.getCountOfApiCallListGroups()
end
