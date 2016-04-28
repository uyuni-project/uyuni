require 'owasp_zap'
require 'nokogiri'
require 'uri'
require 'pp'
require 'erb'
require 'set'
require 'stringio'

include OwaspZap

When(/^the testsuite was run through ZAP as proxy$/) do
  raise "ZAP not running" unless $zap
end

And(/^an active attack was performed$/) do
  if ENV['ZAP_ACTIVE_ATTACK']
    active_scanner = $zap.ascan
    active_scanner.start
    sleep 10 while active_scanner.running?
  end
end

Then(/^there are not security issues$/) do
  json = $zap.alerts.view
  xml = $zap.alerts.view("XML")
  html_res = $zap.alerts.view("HTML")
  File.open("/root/sec_results.html", "w") { |file| file.write(html_res) }
  alerts = []
  unique_alerts = Set.new
  ignored_alerts = Set.new

  if File.exist?('zap_ignored.txt')
    File.open('zap_ignored.txt') do |f|
      f.each_line do |line|
        ignored_alerts.add line.strip
      end
    end
  end

  doc = Nokogiri::XML(xml)
  doc.xpath('/alerts/alert').each do |alert_e|
    alert = {}
    alert_e.children.each do |prop|
      alert[prop.name.to_sym] = prop.text
    end

    url = URI.parse(alert[:url])
    alert[:group] = case url.path
      when /\.do$/ then 'Struts'
      when /\.pxt$/ then 'Perl'
      when /\.dwr$/ then 'DWR'
      else 'Others'
    end

    if url.path
      url.path = url.path.gsub(/;jsessionid=[A-Za-z0-9_]+/, ';jsessionid=' + ('X'*32))
      alert[:url] = url.to_s
    end

    alert[:param] = alert[:param].gsub(/list_\d+_([A-Za-z0-9]+)/, 'list_' + 'X'*10 + '_\1')

    # only insert alerts for the unique id for
    # url-param-alert attributes, to remove duplicates
    # params is normalized to remove changing ids (XXXXXX)
    unique_key = "#{alert[:url]},#{alert[:param]},#{alert[:alert]}"
    alert[:unique_key] = unique_key
    unless unique_alerts.include?(unique_key)
      unique_alerts.add(unique_key)
      alerts << alert
    end
  end

  File.open('zap_all.txt', 'w') do |f|
    unique_alerts.each do |id|
      f.puts id
    end
  end

  html = StringIO.new

  DISPLAY_ATTRS = [:url, :param, :evidence, :description, :solution, :reference]

  grouped = alerts.group_by { |alert| alert[:group] }
  grouped.each do |group, alert_group|
    html << "<h3>#{group}</h3>"
    new_alerts = (unique_alerts - ignored_alerts)
    alert_group.each do |alert|
      next unless new_alerts.include?(alert[:unique_key])

      html << "<h2>#{alert[:alert]}</h2>"
      html << "<ul>"
      DISPLAY_ATTRS.each do |k|
        html <<  "<li>#{k}: #{alert[k]}</li>"
      end
      html << "</ul>"
    end
  end

  grouped.each do |group, alert_group|
    html << "<h3>#{group}</h3>"
    new_alerts = (unique_alerts - ignored_alerts)
    alert_group.each do |alert|
      next unless ignored_alerts.include?(alert[:unique_key])
      html << "<div style='opacity: 0.5; font-size: 0.8em'>"
      html << "<h2>#{alert[:alert]}</h2>"
      html << "<ul>"
      DISPLAY_ATTRS.each do |k|
        html <<  "<li>#{k}: #{alert[k]}</li>"
      end
      html << "</ul>"
      html << "</div>"
    end
  end

  puts html.string
  fail unless (unique_alerts - ignored_alerts).empty?
end
