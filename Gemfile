source 'https://rubygems.org'

gem "dbi", "~> 0.4.5"
gem "cucumber","~> 1.3.0"
gem "capybara","~> 2.1.0"
gem "selenium-webdriver"
gem "rubyzip"

ENV['DB'] ||= "pg" # default postgresql

if ENV['DB'] == "pg"
    gem "pg", "~> 0.11.0" 
else
    gem "ruby-oci8", "~> 2.0.4" 
end

gem 'owasp_zap'
