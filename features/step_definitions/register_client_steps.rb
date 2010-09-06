

Given /^I am root$/ do
 uid = `id -u`
 if ! $?.success? || uid.to_i != 0
   raise "You are not root!" 
 end
 hostname = `hostname`
 hostname.chomp!
 if ! $?.success? || hostname == "linux"
   raise "Invalid hostname"
 end
end

When /^I register using "([^"]*)" key$/ do |arg1|
  regurl = "#{ENV['TESTHOST']}/XMLRPC"
  regurl.sub!(/^https/, "http")

  command = "rhnreg_ks --serverUrl=#{regurl} --activationkey=#{arg1}"
  #print "Command: #{command}\n"
  
  output = `#{command}`
  if ! $?.success?
    raise "Registration failed '#{command}' #{$!}: #{output}"
  end
end

Then /^I should see this client in spacewalk$/ do
  Given "I am on the Systems page"
  hostname = `hostname`
  hostname.chomp!
  Then "I should see a \"#{hostname}\" link"
end

