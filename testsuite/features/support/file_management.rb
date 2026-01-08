# Copyright (c) 2023-2025 SUSE LLC
# Licensed under the terms of the MIT license.

require 'net/http'

# This function tests whether a file exists on a node
# Checks if a file exists on the given node.
#
# @param node [Node] The node on which to check the file existence.
# @param file [String] The path of the file to check.
# @return [Boolean] Returns true if the file exists, false otherwise.
def file_exists?(node, file)
  node.file_exists?(file)
end

# Deletes a file on the specified node.
#
# @param [Node] node The node on which the file should be deleted.
# @param [String] file The path of the file to be deleted.
# @return [void]
def file_delete(node, file)
  node.file_delete(file)
end

# Checks if a folder exists on the given node.
#
# @param node [Node] The node to check for the folder.
# @param file [String] The path of the folder to check.
# @return [Boolean] Returns true if the folder exists, false otherwise.
def folder_exists?(node, file)
  node.folder_exists?(file)
end

# Deletes a folder on the specified node.
#
# @param node [Node] The node on which the folder should be deleted.
# @param folder [String] The name of the folder to be deleted.
# @return [Integer] The exit code of the operation.
def folder_delete(node, folder)
  node.folder_delete(folder)
end

# This function extracts a file from a node
# Extracts a remote file to a local file on the specified node.
#
# @param node [Node] The node on which the file extraction will be performed.
# @param remote_file [String] The path of the remote file to be extracted.
# @param local_file [String] The path of the local file to which the remote file will be extracted.
def file_extract(node, remote_file, local_file)
  node.extract(remote_file, local_file)
end

# Injects a local file into a remote node.
#
# @param [Node] node The remote node to inject the file into.
# @param [String] local_file The path to the local file to be injected.
# @param [String] remote_file The path to the remote file where the local file will be injected.
# @return [void]
def file_inject(node, local_file, remote_file)
  node.inject(local_file, remote_file)
end

# Generates a temporary file with the given name and content.
#
# @param name [String] The name of the temporary file.
# @param content [String] The content to be written to the temporary file.
# @return [File] The Tempfile instance.
def generate_temp_file(name, content)
  file = Tempfile.new(name)
  file.write(content)
  file.flush
  file
end

# Create salt pillar file in the default pillar_roots location
#
# @param source [String] The path of the source file.
# @param file [String] The name of the destination file.
# @return [Integer] The return code indicating the success or failure of the file injection.
def inject_salt_pillar_file(source, file)
  pillars_dir = '/srv/pillar/'
  dest = File.join(pillars_dir, file)
  success = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless success

  # make file readable by salt
  get_target('server').run("chown -R salt:salt #{dest}")
  success
end

# Reads the value of a variable from a given file on a given host
#
# @param host [String] The hostname or IP address of the target host.
# @param file_path [String] The path to the configuration file on the target host.
# @param variable_name [String] The name of the variable to retrieve.
# @return [String] The value of the variable.
# @raise [ScriptError] If reading the variable from the file fails.
def get_variable_from_conf_file(host, file_path, variable_name)
  node = get_target(host)
  variable_value, return_code = node.run("sed -n 's/^#{variable_name} = \\(.*\\)/\\1/p' < #{file_path}")
  raise ScriptError, "Reading #{variable_name} from file on #{host} #{file_path} failed" unless return_code.zero?

  variable_value.strip!
end

# Attempts to retrieve the SHA256 checksum for a file inside a given directory or download it
# from the same domain the file has been downloaded from.
#
# @param dir [String] The directory where the file is located.
# @param original_file_name [String] The original name of the file.
# @param file_url [String] The URL of the file.
# @return [String] The path of the SHA256 checksum file.
def get_checksum_path(dir, original_file_name, file_url)
  checksum_file_names = %W[CHECKSUM SHA256SUMS sha256sum.txt #{original_file_name}.CHECKSUM #{original_file_name}.sha256]

  server = get_target('server')
  cmd = "ls -1 #{dir}"
  # when using a mirror, the checksum file should be present and in the same directory of the file
  if $mirror
    output, _code = server.run(cmd, runs_in_container: false)
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
      _output, code = server.run("cd #{dir} && curl --insecure #{checksum_url} -o #{name}", runs_in_container: false, timeout: 10)
      return "#{dir}/#{name}" if code.zero?
    end

    raise "No SHA256 checksum file to download found for file at #{file_url}"

  end
end

# Computes the SHA256 checksum for the file at the given path and verifies it against a checksum file.
# The original file name is used to retrieve the correct checksum entry
#
# @param original_file_name [String] The name of the original file.
# @param file_path [String] The path to the file to validate.
# @param checksum_path [String] The path to the checksum file.
# @return [Boolean] Returns true if the file's checksum matches the checksum in the checksum file, false otherwise.
def checksum_with_file_valid?(original_file_name, file_path, checksum_path)
  # search the checksum file for what should be the only non-comment line containing the original file name
  cmd = "grep -v '^#' #{checksum_path} | grep '#{original_file_name}'"
  checksum_line, _code = get_target('server').run(cmd, runs_in_container: false)
  raise "SHA256 checksum entry for #{original_file_name} not found in #{checksum_path}" unless checksum_line

  # this relies on the fact that SHA256 hashes have a fixed length of 64 hexadecimal characters to extract the checksum
  # and address any issue related to a checksum file having a different internal format compared to the standard
  checksum_match = checksum_line.match(/\b([0-9a-fA-F]{64})\b/)
  raise "SHA256 checksum not found in entry: #{checksum_line}" unless checksum_match

  expected_checksum = checksum_match[1]
  checksum_valid?(file_path, expected_checksum)
end

# Computes the SHA256 checksum of the file at the given path and returns a boolean representing whether it
# matches the expected checksum or not
#
# @param file_path [String] The path to the file.
# @param expected_checksum [String] The expected checksum value.
# @return [Boolean] Returns true if the file's checksum matches the expected checksum, false otherwise.
def checksum_valid?(file_path, expected_checksum)
  cmd = "sha256sum -b #{file_path} | awk '{print $1}'"
  file_checksum, _code = get_target('server').run(cmd, runs_in_container: false)
  file_checksum.strip == expected_checksum
end

# This function checks if a given repository URL is a development repository
#
# @param repo_url [String] The URL of the repository to check.
# @return [Boolean] Returns true if the repository is a development repository, false otherwise.
def devel_repo?(repo_url)
  url = repo_url.downcase
  (url.include?('devel') || url.include?('systemsmanagement')) && !url.include?('sle-module')
end
