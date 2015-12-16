require 'jwt'
require 'securerandom'

def token(secret, org, channels)
  payload = { org: org }
  if channels && !channels.empty?
    payload.merge!(onlyChannels: channels)
  end
  puts secret
  JWT.encode payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256'
end

def server_secret
  rhnconf = sshcmd('cat /etc/rhn/rhn.conf')[:stdout]
  data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
  return data[1].strip
end

Given(/^I have a valid token for organization "([^"]*)"$/) do |arg1|
  @token = token(server_secret, 1, nil)
end

Given(/^I have an invalid token for organization "([^"]*)"$/) do |arg1|
  @token = token(SecureRandom.hex(64), 1, nil)
end
