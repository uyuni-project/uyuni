require 'rubygems'
require 'yaml'
require 'cucumber/rake/task'
require 'rdoc/task'
require 'rake/testtask'
require 'rake/clean'
require 'owasp_zap'

include OwaspZap

$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)


ENV['LD_LIBRARY_PATH'] = "/usr/lib64/oracle/10.2.0.4/client/lib/"
outputfile = ENV.key?('RUNID') ? "#{ENV['RUNID']}-cucumber-results_#{ENV['ARCH2']}.html" : "output.html"

Dir.glob(File.join(Dir.pwd, 'run_sets', '*.yml')).each do |entry|
  namespace :cucumber do
    Cucumber::Rake::Task.new(File.basename(entry, '.yml').to_sym) do |t|
      cucumber_opts = %W(--format pretty --format html -o #{outputfile} -f rerun --out failed.txt)
      features = YAML.load(File.read(entry))
      t.cucumber_opts = cucumber_opts + features
    end
  end
end

task :cucumber do |t|
  Rake::Task['cucumber:testsuite'].invoke
end

namespace :cucumber do
  task :headless do
    raise "install xorg-x11-server-extra" unless File.exist?("/usr/bin/Xvfb")

    ENV["DISPLAY"] = ":98"
    arglist = ["Xvfb", "#{ENV['DISPLAY']}" ">& Xvfb.log &"]
    pid = fork do
      trap("SIGINT", "IGNORE")
      exec(*arglist)
    end

    until File.exist?("/tmp/.X98-lock")
      STDERR.puts "waiting for virtual X server to settle.."
      sleep 1
    end
    trap("SIGINT") do
      Process.kill("HUP", pid)
    end
    Rake::Task["cucumber"].invoke
  end
end

task :build do
  system "gem build spacewalk_testsuite_base.gemspec"
end

task :install => :build do
  system "sudo gem install spacewalk_testsuite_base.gem"
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
    rdoc.title = "Spacewalk Testsuite"
    extra_docs.each { |ex| rdoc.rdoc_files.include ex }
  end
end

task :default => [:cucumber]
