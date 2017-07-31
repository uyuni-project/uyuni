# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'timeout'

# Class for generating expect files to interactively run spacewalk-push-register.
class ExpectFileGenerator
  def initialize(host, bootstrap)
    raise(ArgumentError, "Hostname is missing!") if host.nil? || host.empty?
    Tempfile.open('push-registration.expect') do |f|
      @file = f
      f.write("spawn spacewalk-ssh-push-init --client " + host + " --register " + bootstrap + " --tunnel" + "\n")
      f.write("while {1} {\n")
      f.write("  expect {\n")
      f.write("    eof                                                        {break}\n")
      f.write("    \"Are you sure you want to continue connecting (yes/no)?\" {send \"yes\r\"}\n")
      f.write("    \"Password:\"                                              {send \"linux\r\"}\n")
      f.write("  }\n")
      f.write("}\n")
    end
  end

  # Return the file path.
  def path
    @file.path
  end

  # Return the file name.
  def filename
    File.basename(path)
  end
end


#
# Function that loop and wait for a success action, otherwise keep sleep until timeout is
# reached and will fail.
#

def waitOrFail(cmd, host)
  node = get_target(host)
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
	puts "running #{cmd}"
	out, code = node.run(cmd, "false")
	puts out
        break if code.nonzero?
        sleep(1)
      end
    end
  rescue Timeout::Error
    puts "timeout reached! something went wrong!"
  end
end

