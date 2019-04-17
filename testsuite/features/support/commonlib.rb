# Copyright (c) 2013-2019 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'tempfile'

# return current URL
def current_url
  driver.current_url
end

# generate temporary file on the controller
def generate_temp_file(name, content)
  Tempfile.open(name) do |file|
    file.write(content)
    return file.path
  end
end

# get registration URL
# the URL depends on whether we use a proxy or not
def registration_url
  if $proxy.nil?
    "https://#{$server.ip}/XMLRPC"
  else
    "https://#{$proxy.ip}/XMLRPC"
  end
end

def count_table_items
  # count table items using the table counter component
  items_label_xpath = "//span[contains(text(), 'Items ')]"
  raise unless (items_label = find(:xpath, items_label_xpath).text)
  items_label.split('of ')[1]
end

# This function creates salt pillar file in the default pillar_roots location
def inject_salt_pillar_file(source, file)
  dest = '/srv/pillar/' + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # make file readeable by salt
  $server.run("chgrp salt #{dest}")
  return_code
end
