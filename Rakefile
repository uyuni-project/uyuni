require 'rubygems'
require 'cucumber/rake/task'
require "rake/rdoctask"
require "rake/testtask"
require "rake/clean"

$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require "spacewalk_testsuite_base/version"

ENV['LD_LIBRARY_PATH'] = "/usr/lib64/oracle/10.2.0.4/client/lib/"

features_task = Cucumber::Rake::Task.new do |t|
  cucumber_opts = %w{--format pretty}
  #cucumber_opts = cucumber_opts + %w{-o /tmp/cucumber.log}
  feature_files  = %w{
                     features/database.feature
                     features/init_user_create.feature
                     features/running.feature
                     features/login.feature
                     features/mainpage.feature
                     features/channels_add.feature
                     features/push_package.feature
                     features/create_repository.feature
                     features/systemspage.feature
                     features/create_activationkey.feature
                     features/users.feature
                     features/users-createnewuser.feature
                     features/users-userdetails.feature
                     features/create_config_channel.feature
                     features/register_client.feature
                     features/monitoring.feature
                     features/system_configuration.feature
                     features/custom_system_info.feature
                     features/create_group.feature
                     features/add_sys_of_group_to_SSM.feature
                     features/configuration.feature
                     features/add_system_to_conf.feature
                     features/walk_hrefs.feature
                     features/delete_system_from_conf.feature
                     features/mgr-bootstrap.feature
                     features/channels.feature
                     features/weak_deps.feature
                     features/check_registration.feature
		     features/check_errata-npn.feature
                     features/erratapage.feature
                     features/install_package.feature
                     features/install_errata-npn.feature
                     features/clone_channel-npn.feature
                     features/monitoring2.feature
                     features/test_config_channel.feature
                     features/ncc-sync-channels.feature
                     features/xmlrpc_system.feature
                     features/delete_system_profile.feature
                     features/delete_config_channel.feature
                     features/users-deleteuser1.feature
                     features/xmlrpc_api.feature
                     features/xmlrpc_activationkey.feature
                     features/xmlrpc_channel.feature
                     features/spacewalk-debug.feature
                    }
  t.cucumber_opts = cucumber_opts + feature_files
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
