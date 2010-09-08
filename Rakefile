require 'rubygems'
require 'cucumber/rake/task'
require "rake/rdoctask"
require "rake/testtask"
require "rake/clean"

$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require "spacewalk_testsuite_base/version"

Cucumber::Rake::Task.new do |t|
  cucumber_opts = %w{--format pretty}
  feature_list  = %w{
                     features/init_user_create.feature
                     features/running.feature
                     features/login.feature
                     features/mainpage.feature
                     features/systemspage.feature
		     features/create_activationkey.feature
		     features/register_client.feature
                     features/system_configuration.feature
		     features/users-createnewuser.feature
		     features/users.feature
                     features/create_group.feature
                     features/add_sys_of_group_to_SSM.feature
                     features/channels_add.feature
                     features/channels.feature
                     features/configuration.feature
                     features/walk_hrefs.feature
                    }
  t.cucumber_opts = cucumber_opts + feature_list
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
