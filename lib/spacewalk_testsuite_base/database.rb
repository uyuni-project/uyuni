# generic low-level access to the database

$dbuser = ENV['DBUSER'] || "spacewalk"
$dbpass = ENV['DBPASS'] || "spacewalk"

$dbhost = ENV['TESTHOST'] || "andromeda.suse.de"
# remove http*://
$dbhost.gsub!(/https?:\/\//, '')

require File.join(File.dirname(__FILE__),'oracle')

# sometime in the future
#require File.join(File.dirname(__FILE__),'postgresql')
