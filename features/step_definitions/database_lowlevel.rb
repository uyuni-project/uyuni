# low-level access to the database

class Database
  require File.join(File.dirname(__FILE__), "..", "..", "lib", "spacewalk_testsuite_base", "database")
  include DatabaseLowLevelAccess
end

db = Database.new

Given /^I have low\-level access to the database$/ do
  @dbh = db.get_dbi_connection()
  raise unless @dbh
  @dbh.disconnect
end

When /^I remove the admin user$/ do
  @dbh = db.get_dbi_connection()
  raise "Can't connect to database" unless @dbh
  admin = nil
  @dbh.execute("SELECT user_id FROM rhnusergroupmembers WHERE user_group_id = ?", "6") do |sth|
    row = sth.fetch
    admin = row[0]
    $stderr.puts "Admin at id #{admin}"
  end
  raise "Can't read from RHNUSERGROUPMEMBERS" unless admin
  @dbh.disconnect
end
