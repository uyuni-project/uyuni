# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.
require 'redis'
require 'nokogiri'
require 'open-uri'

# CodeCoverage handler to produce, parse and report Code Coverage from the Jave Server to our GitHub PRs
class CodeCoverage
  include(Nokogiri::XML)
  ##
  # Initialize a connection with a Redis database
  def initialize(redis_host, redis_port, redis_username, redis_password)
    @database = Redis.new(host: redis_host, port: redis_port, username: redis_username, password: redis_password)
  end

  ##
  # Close the connection with the Redis database
  def close
    @database.close
  end

  ##
  # Parse a JaCoCo XML report, extracting information that will be included in a Set on a Redis database
  #
  # Args:
  #   feature_name: The feature name of the code coverage report we want to parse and push to Redis
  def push_feature_coverage(feature_name)
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

          begin
            @database.sadd("#{package_name}/#{sourcefile_name}", feature_name)
          rescue StandardError => e
            warn("#{e.backtrace} > #{package_name}/#{sourcefile_name} : #{feature_name}")
          end
        end
      end
    rescue StandardError => e
      warn(e.backtrace)
    ensure
      File.delete(filename)
    end
  end

  ##
  # Generate a JaCoCo report naming it as the feature name passed by parameter
  # (https://redis.io/docs/data-types/sets/)
  #
  # Args:
  #   feature_name: The feature name of the code coverage report we want to generate
  #   html: A boolean to export it as HTML
  #   xml: A boolean to export it as XML
  #   source: The path of the class files of the product (Jacoco will provide extra information if we provide it)
  def jacoco_dump(feature_name, html = false, xml = true, source = false)
    cli = 'java -jar /tmp/jacococli.jar'
    html_report = html ? "--html /srv/www/htdocs/pub/jacoco-#{feature_name}" : ''
    xml_report = xml ? "--xml /srv/www/htdocs/pub/jacoco-#{feature_name}.xml" : ''
    sourcefiles = source ? '--sourcefiles /tmp/uyuni-master/java/code/src' : ''
    classfiles = '--classfiles /srv/tomcat/webapps/rhn/WEB-INF/lib/rhn.jar'
    dump_path = "/tmp/jacoco-#{feature_name}.exec"
    # TODO: Refactor test code to run it on a containerized server
    # Inject JaCoCo CLI from the Server Host to the Server container
    # Restart the server to start collecting coverage
    # Inject uyuni_master.zip into the server container and unzip it in /tmp (and remove the same step from the Jenkins pipeline)
    get_target('server').run("#{cli} dump --address localhost --destfile #{dump_path} --port 6300 --reset")
    get_target('server').run("#{cli} report #{dump_path} #{html_report} #{xml_report} #{sourcefiles} #{classfiles}")
    file_extract(get_target('server'), "/srv/www/htdocs/pub/jacoco-#{feature_name}.xml", "/tmp/jacoco-#{feature_name}.xml")
  end
end
