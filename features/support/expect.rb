# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'tempfile'

# Class for generating expect files to interactively run spacewalk-push-register.
#
class ExpectFileGenerator

  # Constructor.
  #
  def initialize(host, bootstrap)
    @host = host
    if !@host
      raise Exception, "Hostname is missing!"
    end
    @file = Tempfile.new('push-registration.expect')
    @file.write("spawn spacewalk-push-register " + host + " " + bootstrap + "\n")
    @file.write("expect \"Are you sure you want to continue connecting (yes/no)?\"\n")
    @file.write("send \"yes\\r\"\n")
    @file.write("expect \"Password:\"\n")
    @file.write("send \"linux\\r\"\n")
    @file.write("expect eof")
    @file.close
  end

  # Return the file path.
  #
  def getPath()
    return @file.path;
  end

  # Return the file name.
  #
  def getFilename()
    return File.basename(getPath());
  end

  # Delete the file.
  #
  def delete()
    @file.unlink
  end
end

