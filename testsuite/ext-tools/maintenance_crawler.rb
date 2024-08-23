#!/usr/bin/env ruby

# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# source 'https://rubygems.org'
#
# gem 'nokogiri'

require 'open-uri'
require 'optparse'
require 'monitor'
require 'nokogiri'

# Maintenance Updates Crawler class
class MaintenanceCrawler
  # Create a new crawler for the given root_url
  def initialize(root_url, options = {})
    @verbose = options[:verbose]
    @architecture = options[:architecture]
    @thread_count = options[:thread_count]
    @thread_count ||= 1
    @root_url = root_url
  end

  # Perform the site crawl
  # 1. Creates a queue of urls to crawl (starting with the root url)
  # 2. Create a thread pool (using size thread_count, defined when created)
  # 3. While queue not empty, threads will process URLs
  def crawl
    puts "Crawling #{@root_url}" if @verbose
    @pages = {}
    @crawl_queue = Queue.new
    @crawl_queue << @root_url.to_s

    @crawl_queue.extend MonitorMixin
    crawl_queue_cond = @crawl_queue.new_cond

    threads = []
    active_threads = 0
    crawl_complete = false

    @thread_count.times do |_i|
      # Register/count each active thread
      @crawl_queue.synchronize do
        active_threads += 1
      end

      resources = nil
      url = nil

      threads << Thread.new do
        loop do
          # Synchronize on critical code which adds to the pages and queue
          @crawl_queue.synchronize do
            if resources.nil?
              # URL Error, skip. Could add future functionality for n-retries?
              @pages.delete url
            else
              update_pages_and_queue(url, resources)
              print_status(url) if @verbose
            end

            # 1. If empty queue + no other threads running implies that we've
            #    completed the site crawl. Can be modified by all threads
            # 2. Wake up other threads which will either process more urls or
            #    exit depending on 'crawl_complete' and queue state
            # 3. Wait until queue is not empty or crawling is marked as complete
            # 4. Thread has woken up, exit if we're done crawling
            # 5. If not done, bump active thread count and re-enter loop
            crawl_complete = true if @crawl_queue.empty? && (active_threads == 1)
            crawl_queue_cond.broadcast unless @crawl_queue.empty? && !crawl_complete
            active_threads -= 1
            crawl_queue_cond.wait_while { @crawl_queue.empty? and !crawl_complete }
            Thread.exit if crawl_complete
            active_threads += 1

            url = @crawl_queue.shift
          end

          resources = crawl_url url
        end
      end
    end

    threads.each(&:join)
  end

  # Get the pages hash. Each entry contains a hash for the links and assets
  def get_pages
    @pages
  end

  # Print the repos
  def get_repos
    repos = []
    @pages.each do |page|
      page[1][:links].each do |link|
        repos << page[0] if link.match(/repo$/) && page[0].include?("#{@architecture}/")
      end
    end
    repos.uniq
  end

  private

  # Retrieves HTML for the given url, extract all links and assets and return in a hash
  def crawl_url(url)
    begin
      html = Nokogiri::HTML(URI.parse(url).open.read)
    rescue StandardError => e
      puts "Error reading #{url} :: #{e}" if @verbose
      return
    end

    links = html.css('a').map { |link| process_url link['href'] }.compact

    { links: links.uniq }
  end

  def process_url(url)
    return if url.nil? || url.empty? || url.include?('../') || !url.include?('./')

    bad_matches = [%r{^(http(?!#{Regexp.escape @root_url.gsub('http', '')})|//)}, /^mailto/, /^tel/, /^javascript/]

    # Case slightly more open to extension
    case url
    when *bad_matches
      nil
    else
      URI.join(@root_url, url).to_s
    end
  end

  # Output the current completions/total_queued to the console
  # Defaults to single-line-update but verbose (-v) mode triggers full output
  def print_status(url)
    done = @pages.values.compact.length.to_s.rjust(2, '0')
    total = @pages.length.to_s.rjust(2, '0')
    print "\r#{' ' * 80}\r" unless @verbose
    print "Crawled #{done}/#{total}: #{url}"
    print "\n" if @verbose
    $stdout.flush
  end

  # Sets the page resources for the given URL and adds any new links
  # to the crawl queue
  def update_pages_and_queue(url, resources)
    @pages[url] = resources
    resources[:links].each do |link|
      unless @pages.key? link
        @crawl_queue.enq(link)
        @pages[link] = nil
      end
    end
  end
end

# Gather Command Line Options
options = {}
options[:verbose] = false
options[:architecture] = 'x86_64'

opt_parser =
  OptionParser.new do |opt|
    opt.banner = 'Usage: maintenance_crawler list_MI_numbers(separated by comma) [OPTIONS]'
    opt.separator  ''
    opt.separator  'Options'

    opt.on('-t n', '--thread-count=n', OptionParser::DecimalInteger, 'Process using a thread pool of size n') do |thread_count|
      options[:thread_count] = thread_count
    end

    opt.on('-v', '--verbose', 'show all urls processed') do
      options[:verbose] = true
    end

    opt.on('-a', '--architecture=name', 'filter by architecture') do |architecture|
      options[:architecture] = architecture
    end

    opt.on('-h', '--help', 'help (show this)') do
      puts opt_parser
      exit
    end
  end

# Run crawler
opt_parser.parse!

# Require domain
if ARGV.count < 1
  puts opt_parser
  exit
end

# Crawl Maintenance Incidences
mi_numbers = ARGV[0]
mi_numbers.split(',').each do |mi_number|
  c = MaintenanceCrawler.new("http://download.suse.de/download/ibs/SUSE:/Maintenance:/#{mi_number}/", options)
  c.crawl
  puts c.get_repos
end
