require 'jwt'
require 'securerandom'

# Valid claims:
#   - org
#   - onlyChannels
def token(secret, claims={})
  payload = {}
  payload.merge!(claims)
  puts secret
  JWT.encode payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256'
end

def server_secret
  rhnconf = sshcmd('cat /etc/rhn/rhn.conf')[:stdout]
  data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
  data[1].strip
end

Given(/^I have a valid token for organization "([^"]*)"$/) do |org|
  @token = token(server_secret, org: org.to_i)
end

Given(/^I have an invalid token for organization "([^"]*)"$/) do |org|
  @token = token(SecureRandom.hex(64), org: org.to_i)
end

Given(/^I have an expired valid token for organization "([^"]*)"$/) do |org|
  yesterday = Time.now.to_i - 86_400
  @token = token(server_secret, org: org.to_i, exp: yesterday)
end

Given(/^I have a valid token expiring tomorrow for organization "([^"]*)"$/) do |org|
  tomorrow = Time.now.to_i + 86_400
  @token = token(server_secret, org: org.to_i, exp: tomorrow)
end

Given(/^I have a not yet usable valid token for organization "([^"]*)"$/) do |org|
  tomorrow = Time.now.to_i + 86_400
  @token = token(server_secret, org: org.to_i, nbf: tomorrow)
end

Given(/^I have a valid token for organization "(.*?)" and channel "(.*?)"$/) do |org, channel|
  @token = token(server_secret, org: org, onlyChannels: [channel])
end
