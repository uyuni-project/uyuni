class XMLRPCApiTest < XMLRPCBaseTest
  def getVersion()
    return @connection.call("api.getVersion")
  end
end

rpcApiTester = XMLRPCApiTest.new(ENV["TESTHOST"])
testVersion = nil

When /^I call getVersion$/ do
  testVersion = rpcApiTester.getVersion()
end

Then /^I should get "([^"]*)" as result$/ do |version|
  fail if not testVersion == version
end


Then /^I should not get "([^"]*)" as result$/ do |wrongVersion|
  fail if testVersion == wrongVersion
end
