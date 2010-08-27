srcdir = File.dirname(__FILE__)

require 'rubygems'
require 'oci8'
require 'test/unit'
require "#{srcdir}/config"

require "#{srcdir}/test_oradate"
require "#{srcdir}/test_oranumber"
require "#{srcdir}/test_bind_time"
require "#{srcdir}/test_bind_raw"
if $test_clob
  require "#{srcdir}/test_clob"
end

require "#{srcdir}/test_break"
require "#{srcdir}/test_oci8"
require "#{srcdir}/test_datetime"
require "#{srcdir}/test_connstr"
require "#{srcdir}/test_metadata"
require "#{srcdir}/test_array_dml"
require "#{srcdir}/test_rowid"
require "#{srcdir}/test_appinfo"
require "#{srcdir}/test_oracle_version"

if OCI8.respond_to? :encoding
  require "#{srcdir}/test_encoding"
end

# Ruby/DBI
begin
  require 'dbi'
rescue LoadError
  begin
    require 'rubygems'
    require 'dbi'
  rescue LoadError
    dbi_not_found = true
  end
end
unless dbi_not_found
  require "#{srcdir}/test_dbi"
  if $test_clob
    require "#{srcdir}/test_dbi_clob"
  end
end

#Test::Unit::AutoRunner.run(true, true)
if defined? Test::Unit::AutoRunner
  Test::Unit::AutoRunner.run()
end
