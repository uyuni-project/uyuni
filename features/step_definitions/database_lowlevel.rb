# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# low-level access to the database

class Database
  # Set locale to 'C'. Without it, ruby-oci8 hangs in nsgetcinfo() (deep inside the oracle client lib)
  #   Found http://www.nntp.perl.org/group/perl.dbi.users/2010/09/msg35317.html and
  #   http://www.nntp.perl.org/group/perl.dbi.users/2010/09/msg35319.html
  #   hinting towards a locale problem. And setting LC_ALL to C fixed it.
  ENV["LC_ALL"] = "C"
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
  begin
    # get the 'spacewalk superuser'
    @dbh.execute("SELECT user_id FROM rhnusergroupmembers WHERE user_group_id = ?", "6") do |sth|
      row = sth.fetch
      admin = row[0] if row
      $stderr.puts "Admin at id #{admin}"
    end
    if admin
      rows = @dbh.do("DELETE from RHNUSERINFO where user_id = ?", admin)
      $stderr.puts "#{rows} removed from RHNUSERINFO"

      rows = @dbh.do("DELETE from RHNUSERGROUPMEMBERS where user_id = ?", admin)
      $stderr.puts "#{rows} removed from RHNUSERGROUPMEMBERS"

      rows = @dbh.do("UPDATE RHNUSERGROUP set CURRENT_MEMBERS = ? where CURRENT_MEMBERS = ?", 0, 1)
      $stderr.puts "#{rows} updated in RHNUSERGROUP"

      rows = @dbh.do("TRUNCATE TABLE WEB_CONTACT")
      $stderr.puts "#{rows} removed from WEB_CONTACT"

      @dbh.do("COMMIT")
    else
      $stderr.puts "Admin user already removed"
    end

  rescue DBI::DatabaseError => e
    $stderr.puts "An error occurred"
    $stderr.puts "Error code: #{e.err}"
    $stderr.puts "Error message: #{e.errstr}"
    $stderr.puts "Error SQLSTATE: #{e.state}"
    raise "Datbase operation failed"
  ensure
    @dbh.disconnect
  end
end
