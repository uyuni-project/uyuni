# -*- encoding: utf-8 -*-
lib = File.expand_path('../lib/', __FILE__)
$:.unshift lib unless $:.include?(lib)
require "spacewalk_testsuite_base/version"

Gem::Specification.new do |s|
  s.name        = "spacewalk_testsuite_base"
  s.version     = SpacewalkTestsuiteBase::VERSION
  s.platform    = Gem::Platform::RUBY
  s.authors     = ["Novell Inc."]
  s.email       = ["galaxy-devel@suse.de"]
  s.homepage    = "http://git.suse.de"
  s.summary = "Tests for spacewalk installations"
  s.description = "Testsuite to QA installations of Spacewalk and its derivatives"

  s.required_rubygems_version = ">= 1.3.6"
  s.add_development_dependency("pg", ["~> 0.10.1"])
  s.add_development_dependency("ruby-oci8", ["~> 2.0.4"])
  s.add_development_dependency("dbi", ["~> 0.4.5"])
  s.add_development_dependency("cucumber", [">= 0"])
  s.add_development_dependency("capybara", [">= 0"])
  s.add_development_dependency("selenium-webdriver", [">= 0"])

  s.files        = Dir.glob("lib/**/*.rb") + Dir.glob("features/**/*.rb") + Dir.glob("test/**/*.rb") + %w(CHANGELOG.rdoc README.rdoc)
  s.require_path = 'lib'

  s.post_install_message = <<-POST_INSTALL_MESSAGE
  ____
/@    ~-.
\/ __ .- | remember to have fun! 
 // //  @  

  POST_INSTALL_MESSAGE
end
