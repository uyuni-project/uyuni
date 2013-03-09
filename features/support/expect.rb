# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'tempfile'

# Class for generating expect files to interactively run spacewalk-push-register.
#
class ExpectFileGenerator

  # Constructor.
  #
  def initialize(host, bootstrap)
    raise(ArgumentError, "Hostname is missing!") if host.nil? || host.empty?
    Tempfile.open('push-registration.expect') do |f|
      @file = f
      f.write("spawn spacewalk-push-register " + host + " " + bootstrap + "\n")
      f.write("while {1} {\n")
      f.write("  expect {\n")
      f.write("    eof                                                      {break}\n")
      f.write("    "Are you sure you want to continue connecting (yes/no)?" {send \"yes\r\"}\n")
      f.write("    "Password:"                                              {send \"linux\r\"}\n")
      f.write("  }\n")
      f.write("}\n")
    end
  end

  # Return the file path.
  #
  def path
    @file.path
  end

  # Return the file name.
  #
  def filename
    File.basename(path)
  end

  # Delete the file.
  #
  def delete
    @file.unlink
  end
end

