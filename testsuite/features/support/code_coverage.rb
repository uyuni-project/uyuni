# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.
require_relative 'keyvalue_store'
require 'nokogiri'
require 'open-uri'

# CodeCoverage handler to produce, parse and report Code Coverage from the Java Server to our GitHub PRs
class CodeCoverage
  include Nokogiri::XML

  # Initialize the CodeCoverage handler
  def initialize
    @keyvalue_store = KeyValueStore.new(ENV.fetch('REDIS_HOST', nil), ENV.fetch('REDIS_PORT', nil), ENV.fetch('REDIS_USERNAME', nil), ENV.fetch('REDIS_PASSWORD', nil))
  end

  # Parse a JaCoCo XML report, extracting information that will be included in a Set on a Redis database
  #
  # @param feature_name [String] The name of the feature.
  def push_feature_coverage(feature_name)
    $stdout.puts("Pushing coverage for #{feature_name} into Redis")
    filename = "/tmp/jacoco-#{feature_name}.xml"
    begin
      xml = File.read(filename, mode: 'r')
      options = Nokogiri::XML::ParseOptions.new(ParseOptions::HUGE | ParseOptions::RECOVER | ParseOptions::NONET)
      tree = Nokogiri::XML::Document.parse(xml, nil, nil, options)
      tree.xpath('.//package').each do |package|
        package_name = package.attr('name')
        package.xpath('.//sourcefile').each do |sourcefile|
          sourcefile_name = sourcefile.attr('name')
          counter_class = sourcefile.xpath('.//counter[@type=\'CLASS\']')
          next if counter_class.nil? || counter_class.attr('covered').to_s == ''

          next unless Integer(counter_class.attr('covered').to_s).positive?

          @keyvalue_store.add("#{package_name}/#{sourcefile_name}", feature_name)
        end
      end
    rescue StandardError => e
      warn(e.backtrace)
    ensure File.delete(filename)
    end
  end

  # Generate a JaCoCo report naming it as the feature name passed by parameter
  # (https://redis.io/docs/data-types/sets/)
  #
  # @param feature_name [String] The name of the feature.
  # @param html [Boolean] Whether to generate an HTML report (default: false).
  # @param xml [Boolean] Whether to generate an XML report (default: true).
  # @param source [Boolean] Whether to include source files in the report (default: false).
  def jacoco_dump(feature_name, html = false, xml = true, source = false)
    cli = 'java -jar /tmp/jacococli.jar'
    html_report = html ? "--html /srv/www/htdocs/pub/jacoco-#{feature_name}" : ''
    xml_report = xml ? "--xml /srv/www/htdocs/pub/jacoco-#{feature_name}.xml" : ''
    sourcefiles = source ? '--sourcefiles /tmp/uyuni-master/java/core/src/main/java' : ''
    classfiles = '--classfiles /srv/tomcat/webapps/rhn/WEB-INF/lib/rhn.jar'
    dump_path = "/var/cache/jacoco-#{feature_name}.exec"
    get_target('server').run("#{cli} dump --address localhost --destfile #{dump_path} --port 6300 --reset", verbose: true)
    get_target('server').run("#{cli} report #{dump_path} #{html_report} #{xml_report} #{sourcefiles} #{classfiles}", verbose: true)
    get_target('server').extract("/srv/www/htdocs/pub/jacoco-#{feature_name}.xml", "/tmp/jacoco-#{feature_name}.xml")
  end
end
