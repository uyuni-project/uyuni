require 'rubygems'
require 'yaml'
require 'cucumber/rake/task'
require "rake/rdoctask"
require "rake/testtask"
require "rake/clean"

$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require "spacewalk_testsuite_base/version"

ENV['LD_LIBRARY_PATH'] = "/usr/lib64/oracle/10.2.0.4/client/lib/"

Dir.glob(File.join(Dir.pwd, 'run_sets', '*.yml')).each do |entry|
  namespace :cucumber do
    Cucumber::Rake::Task.new(File.basename(entry, '.yml').to_sym) do |t|
      cucumber_opts = %w{--format pretty}
      features = YAML::load(File.read(entry))
      t.cucumber_opts = cucumber_opts + features
    end
  end
end

task :cucumber do |t|
  Rake::Task['cucumber:testsuite'].invoke
end

namespace :cucumber do
  task :headless do
    raise "install xorg-x11-server-extra" if not File.exist?("/usr/bin/Xvfb")

    ENV["DISPLAY"] = ":98"
    arglist = ["Xvfb", "#{ENV["DISPLAY"]}" ">& Xvfb.log &"]
    pid = fork do
      trap("SIGINT", "IGNORE")
      exec(*arglist)
    end

    while ! File.exist?("/tmp/.X98-lock")
      STDERR.puts "waiting for virtual X server to settle.."
      sleep 1
    end
    trap ("SIGINT") do
      Process.kill("HUP", pid)
    end

    #############################################
    #### Launch a virtual framebuffer X server ###
    ##############################################
    #ENV["DISPLAY"] = ":98"
    #system "Xvfb #{ENV["DISPLAY"]} >& Xvfb.log &"
    #trap("EXIT") do
    #  pid = $!
    #  puts "kill #{pid}"
    #  Process.kill("HUP", pid)
    #end
    #sleep 10
    ## start your application/testsuite here
    Rake::Task["cucumber"].invoke
  end
end

task :build do
  system "gem build spacewalk_testsuite_base.gemspec"
end

task :install => :build do
  system "sudo gem install spacewalk_testsuite_base-#{SpacewalkTestsuiteBase::VERSION}.gem"
end

Rake::TestTask.new do |t|
  t.libs << File.expand_path('../test', __FILE__)
  t.libs << File.expand_path('../', __FILE__)
  t.test_files = FileList['test/**/test*.rb']
  t.verbose = true
end

extra_docs = ['README*', 'CHANGELOG*', 'TESTING_HOWTO*']

begin
 require 'yard'
  YARD::Rake::YardocTask.new(:doc) do |t|
    t.files   = ['lib/**/*.h', 'lib/**/*.c', 'lib/**/*.rb', *extra_docs]
  end
rescue LoadError
  STDERR.puts "Install yard if you want prettier docs"
  Rake::RDocTask.new(:doc) do |rdoc|
    rdoc.rdoc_dir = "doc"
    rdoc.title = "Spacewalk Testsuite #{SpacewalkTestsuiteBase::VERSION}"
    extra_docs.each { |ex| rdoc.rdoc_files.include ex }
  end
end

task :default => [:cucumber]
