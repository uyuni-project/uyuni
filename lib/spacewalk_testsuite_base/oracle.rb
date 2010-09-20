#
# Oracle-specific database access routines
#  See ./database.rb for environment
#
#
# !!! $dbuser must have permission to run DBMS_LOCK.SLEEP
# !!!  connect as sys
# !!!    GRANT EXECUTE ON dbms_lock TO ruby;
#

require 'rubygems'
require 'oci8'
require 'test/unit'

# add the port if needed
if not $dbhost =~ /.+:(\d+)$/
  $dbhost = "#{$dbhost}:1521"
end
$dbname = "//#{$dbhost}/XE"

# test_clob.rb

nls_lang = ENV['NLS_LANG']
nls_lang = nls_lang.split('.')[1] unless nls_lang.nil?
nls_lang = nls_lang.upcase unless nls_lang.nil?
case nls_lang
when 'JA16EUC'
  $lobfile = File.dirname(__FILE__) + '/../doc/api.ja.rd' # EUC-JP file
else
  $lobfile = File.dirname(__FILE__) + '/../doc/api.en.rd' # ASCII file
end
$lobreadnum = 256 # counts in charactors

# don't modify below.

# $oracle_server_version: database compatible level of the Oracle server.
# $oracle_client_version: Oracle client library version for which oci8 is compiled.
# $oracle_version: lower value of $oracle_server_version and $oracle_client_version.
conn = OCI8.new($dbuser, $dbpass, $dbname)
begin
  conn.exec('select value from database_compatible_level') do |row|
    $oracle_server_version = OCI8::OracleVersion.new(row[0])
  end
rescue OCIError
  raise if $!.code != 942 # ORA-00942: table or view does not exist
  $oracle_server_version = OCI8::ORAVER_8_0
end
conn.logoff

if $oracle_server_version < OCI8.oracle_client_version
  $oracle_version = $oracle_server_version
else
  $oracle_version = OCI8.oracle_client_version
end

if $oracle_version < OCI8::ORAVER_8_1
  $test_clob = false
else
  $test_clob = true
end

module DatabaseLowLevelAccess
      def get_oci8_connection()
        OCI8.new($dbuser, $dbpass, $dbname)
      rescue OCIError
        raise if $!.code != 12516 && $!.code != 12520
        # sleep a few second and try again if
        # the error code is ORA-12516 or ORA-12520.
        #
        # ORA-12516 - TNS:listener could not find available handler with
        #             matching protocol stack
        # ORA-12520 - TNS:listener could not find available handler for
        #             requested type of server
        #
        # Thanks to Christopher Jones.
        #
        # Ref: The Underground PHP and Oracle Manual (page 175 in vesion 1.4)
        #      http://www.oracle.com/technology/tech/php/pdf/underground-php-oracle-manual.pdf
        #
        sleep(5)
        OCI8.new($dbuser, $dbpass, $dbname)
      end

      def get_dbi_connection()
        DBI.connect("dbi:OCI8:#{$dbname}", $dbuser, $dbpass, 'AutoCommit' => false)
      rescue DBI::DatabaseError
        raise if $!.err != 12516 && $!.err != 12520
        # same as get_oci8_connection()
        sleep(5)
        DBI.connect("dbi:OCI8:#{$dbname}", $dbuser, $dbpass, 'AutoCommit' => false)
      end

      def drop_table(table_name)
        if $oracle_server_version < OCI8::ORAVER_10_1
          # Oracle 8 - 9i
          sql = "DROP TABLE #{table_name}"
        else
          # Oracle 10g -
          sql = "DROP TABLE #{table_name} PURGE"
        end

        if defined? @conn
          begin
            @conn.exec(sql)
          rescue OCIError
            raise if $!.code != 942 # table or view does not exist
          end
        elsif instance_variable_get(:@dbh)
          begin
            @dbh.do(sql)
          rescue DBI::DatabaseError
            raise if $!.err != 942 # table or view does not exist
          end
        end
      end # drop_table
end # module
    