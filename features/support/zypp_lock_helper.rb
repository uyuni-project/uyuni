# Copyright (c) 2010-2014 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# features/support/zypp_lock_helper.rb
#

def read_zypp_lock_file(lock_file)
  locks = []
  lock  = {}

  File.open(lock_file).each_line do |line|
    next if line.start_with?("#")

    line.strip!
    if line.empty?
      if !lock.keys.empty?
        locks << lock
        lock = {}
      end
    else
      key, value = line.split(":", 2)
      lock[key.strip] = value.strip
    end
  end

  locks << lock unless lock.keys.empty?

  locks
end
