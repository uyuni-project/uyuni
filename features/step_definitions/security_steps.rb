require 'owasp_zap'
require 'nokogiri'
require 'uri'
require 'pp'

include OwaspZap

When(/^the testsuite was run through ZAP as proxy$/) do
  if ENV['SECURITY_PROXY']
    target = "https://#{ENV['TESTHOST']}"
    @zap = Zap.new(:target=> target, :zap => "/usr/share/owasp-zap/zap.sh")
  end
end

And(/^an active attack was performed$/) do
  if ENV['SECURITY_ATTACK']
    active_scanner = @zap.ascan
    while active_scanner.running? do
      sleep 10
    end
  end
end

Then(/^there are not security issues$/) do
  if @zap
    json = @zap.alerts.view
    xml = @zap.alerts.view("HTML")

    alerts = []

    doc = Nokogiri::XML(xml)
    doc.xpath('//alert').each do |alert_e|
      alert = {}
      alert_e.children.each do |prop|
        alert[prop.name.to_sym] = prop.text
      end

      url = URI.parse(alert[:url])
      url.query = url.query.gsub(/;jsessionid=[A-Za-z0-9_]+/, ';jsessionid=' + ('X'*32))

      alert[:url] = url.to_s

      alert[:parameter] = alert[:parameter].gsub(/list_\d+_([A-Za-z0-9]+)/, 'list_' + 'X'*10 + '_\1')
      alerts << alert
    end

    alerts.each do |alert|
      puts PP.pp(alert, '')
    end
  else
    pending
  end
end
