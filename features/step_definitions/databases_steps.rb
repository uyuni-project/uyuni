# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
#
# Author: Bo Maryniuk <bo@suse.de>
#


class DatabaseScenarioCollector
  attr :ora_user, true
  attr :ora_pwd, true
  attr :ora_db, true
  attr :pg_user, true
  attr :pg_pwd, true
  attr :pg_db, true

  #
  # Constructor.
  #
  def initialize(host)
    @host = host
  end


  #
  # Basic test execution.
  #
  def basic_test
    ora_sysdate = DatabaseTester.new(@host).get_oracle_conn(@ora_user, @ora_pwd, @ora_db).test_select_sysdate
    pg_sysdate = DatabaseTester.new(@host).get_postgres_conn(@pg_user, @pg_pwd, @pg_db).test_select_sysdate

    puts "      ORA System Date: " + (ora_sysdate ? ora_sysdate.to_s : "N/A")
    puts "      PostgreSQL System Date: " + (pg_sysdate ? pg_sysdate.to_s : "N/A")

    return (ora_sysdate or pg_sysdate)
  end
end


dsc = DatabaseScenarioCollector.new(ENV["TESTHOST"])


Then /^I define user "([^"]*)" for Oracle DB$/ do |ora_user|
  dsc.ora_user = ora_user
end

Then /^I define password "([^"]*)" for Oracle DB$/ do |ora_pwd|
  dsc.ora_pwd = ora_pwd
end

Then /^I select the database "([^"]*)" for Oracle DB$/ do |ora_db|
  dsc.ora_db = ora_db
end

Then /^I define user "([^"]*)" for Postgres$/ do |pg_user|
  dsc.pg_user = pg_user
end

Then /^I define password "([^"]*)" for Postgres$/ do |pg_pwd|
  dsc.pg_pwd = pg_pwd
end

Then /^I select the database "([^"]*)" for Postgres$/ do |pg_db|
  dsc.pg_db = pg_db
end

Then /^I should connect and see at least one result back$/ do
  fail if not dsc.basic_test
end
