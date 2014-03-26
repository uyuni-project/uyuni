source 'https://rubygems.org'

ENV['DB'] ||= "pg" # default postgresql

if ENV['DB'] == "pg"
    gem "pg", "~> 0.11.0" 
else
    gem "ruby-oci8", "~> 2.0.4" 
end
#TODO gemspec is necessary just for gems, not for application
gemspec
