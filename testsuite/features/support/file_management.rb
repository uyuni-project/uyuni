# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

require 'net/http'

# This function tests whether a file exists on a node
def file_exists?(node, file)
  node.file_exists(file)
end

# This function deletes a file from a node
def file_delete(node, file)
  node.file_delete(file)
end

# This function tests whether a folder exists on a node
def folder_exists?(node, file)
  node.folder_exists(file)
end

# This function deletes a file from a node
def folder_delete(node, folder)
  node.folder_delete(folder)
end

# This function extracts a file from a node
def file_extract(node, remote_file, local_file)
  node.extract(remote_file, local_file, 'root', false)
end

# This function injects a file into a node
def file_inject(node, local_file, remote_file)
  node.inject(local_file, remote_file, 'root', false)
end

# Generate temporary file on the controller
def generate_temp_file(name, content)
  Tempfile.open(name) do |file|
    file.write(content)
    return file.path
  end
end

# Create salt pillar file in the default pillar_roots location
def inject_salt_pillar_file(source, file)
  dest = "/srv/pillar/#{file}"
  return_code = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless return_code.zero?

  # make file readable by salt
  get_target('server').run("chgrp salt #{dest}")
  return_code
end

# Reads the value of a variable from a given file on a given host
def get_variable_from_conf_file(host, file_path, variable_name)
  node = get_target(host)
  variable_value, return_code = node.run("sed -n 's/^#{variable_name} = \\(.*\\)/\\1/p' < #{file_path}")
  raise ScriptError, "Reading #{variable_name} from file on #{host} #{file_path} failed" unless return_code.zero?

  variable_value.strip!
end

# Attempts to retrieve the SHA256 checksum for a file inside a given directory or download it
# from the same domain the file has been downloaded from.
# Returns the path to the checksum file.
def get_checksum_path(dir, original_file_name, file_url)
  checksum_file_names = %W[CHECKSUM SHA256SUMS sha256sum.txt #{original_file_name}.CHECKSUM #{original_file_name}.sha256]

  server = get_target('server')
  cmd = "ls -1 #{dir}"
  # when using a mirror, the checksum file should be present and in the same directory of the file
  if $mirror
    output, _code = $is_containerized_server ? server.run_local(cmd) : server.run(cmd)
    files = output.split("\n")
    checksum_file = files.find { |file| checksum_file_names.include?(file) }

    raise "SHA256 checksum file not found in #{dir}" unless checksum_file

    "#{dir}/#{checksum_file}"
  # attempt to download the checksum file
  else
    base_url = file_url.delete_suffix(original_file_name)
    uri = URI.parse(base_url)

    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = (uri.scheme == 'https')
    base_path = uri.path

    checksum_file_names.each do |name|
      # check the URL for the checksum file actually exists and if it does download it on the server
      checksum_path = base_path + name
      request = Net::HTTP::Head.new(checksum_path)
      response = http.request(request)
      next unless response.is_a?(Net::HTTPSuccess)

      checksum_url = base_url + name
      _output, code = server.run("cd #{dir} && curl --insecure #{checksum_url}-o #{name}", timeout: 10)
      return "#{dir}/#{name}" if code.zero?
    end

    raise "No SHA256 checksum file to download found for file at #{file_url}"

  end
end

# Computes the SHA256 checksum for the file at the given path and verifies it against a checksum file.
# The original file name is used to retrieve the correct checksum entry
def validate_checksum_with_file(original_file_name, file_path, checksum_path)
  # search the checksum file for what should be the only non-comment line containing the original file name
  cmd = "grep -v '^#' #{checksum_path} | grep '#{original_file_name}'"
  checksum_line, _code = $is_containerized_server ? get_target('server').run_local(cmd) : get_target('server').run(cmd)
  raise "SHA256 checksum entry for #{original_file_name} not found in #{checksum_path}" unless checksum_line

  # this relies on the fact that SHA256 hashes have a fixed length of 64 hexadecimal characters to extract the checksum
  # and address any issue related to a checksum file having a different internal format compared to the standard
  checksum_match = checksum_line.match(/\b([0-9a-fA-F]{64})\b/)
  raise "SHA256 checksum not found in entry: #{checksum_line}" unless checksum_match

  expected_checksum = checksum_match[1]
  validate_checksum(file_path, expected_checksum)
end

# Computes the SHA256 checksum of the file at the given path and returns a boolean representing whether it
# matches the expected checksum or not
def validate_checksum(file_path, expected_checksum)
  cmd = "sha256sum -b #{file_path} | awk '{print $1}'"
  file_checksum, _code = $is_containerized_server ? get_target('server').run_local(cmd) : get_target('server').run(cmd)
  file_checksum.strip == expected_checksum
end
