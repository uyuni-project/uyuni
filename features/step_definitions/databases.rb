#!/usr/bin/ruby
#
# Author: Bo Maryniuk <bo@suse.de>
#

require 'rubygems'
require 'pg'
require 'dbi'
require 'oci8'

#
# Database direct connection tester class, which original purpose was to use in a Cucumber.
#
class DatabaseTester
  PG_DB = "PostgreSQL DB"
  PG_PORT = 5432
  ORA_DB = "Oracle DB"
  ORA_PORT = 1521
  @vendor = nil


  #
  # Constructor.
  #
  def initialize(host)
    @host = host
    if !@host
      raise Exception, "Hostname is missing!"
    end
    
    @connection = nil
  end


  #
  # Get a connection to an Oracle database.
  # Params:
  # +user+:: username in the database.
  # +password+:: password to the database.
  # +database+:: database name.
  #
  def get_oracle_conn(user, password, database)
    @vendor = DatabaseTester::ORA_DB

    if @host.split(":").length > 1
      @host = @host.sub(":", "") + ":" + DatabaseTester::ORA_PORT.to_s
    end

    db = "//" + @host + "/" + database

    begin
      @connection = OCI8.new(user, password, db)
      puts "      SUCCESS: Connected to " + @vendor + " database."
    rescue Exception => exception
      puts "      ERROR: Can not connect to the " + @vendor + " database. " + exception.message
    end

    return self
  end


  #
  # Get a connection to a PostgreSQL database.
  # Params:
  # +user+:: username in the database.
  # +password+:: password to the database.
  # +database+:: database name.
  #
  def get_postgres_conn(user, password, database)
    @vendor = DatabaseTester::PG_DB

    begin
      @connection = PGconn.open(@host, DatabaseTester::PG_PORT, '', '', database, user, password)
      puts "      SUCCESS: Connected to " + @vendor + " database."
    rescue Exception => exception
      puts "      ERROR: Can not connect to the " + @vendor + " database. " + exception.message
    end

    return self
  end


  #
  # Tests an ability to get a sysdate from the database engine.
  # Returns system date object or null.
  #
  def test_select_sysdate
    sysdate = nil
    if @connection
      result = @connection.exec("SELECT " + ((@vendor == DatabaseTester::ORA_DB) ? "sysdate AS system_date FROM dual" : "now() as system_date"))

      # :-(
      if result and @vendor == DatabaseTester::PG_DB
        result.each do |row|
          sysdate = row["system_date"]
          break
        end
        @connection.close
      elsif result and @vendor == DatabaseTester::ORA_DB
        while row = result.fetch
          sysdate = row[0]
          break
        end
      else
        puts "    ERROR: Unknown DB vendor!"
      end
    end

    return sysdate
  end


end

