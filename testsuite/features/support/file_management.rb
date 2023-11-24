# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

require 'net/http'

# Attempts to retrieve the SHA256 checksum for a file inside a given directory or download it
# from the same domain the file has been downloaded from.
# Returns the path to the checksum file.
def get_checksum_path(dir, original_file_name, file_url)
  checksum_file_names = %W[CHECKSUM SHA256SUMS sha256sum.txt #{original_file_name}.CHECKSUM #{original_file_name}.sha256]

  server = get_target('server')
  # When using a mirror, the checksum file should be present and in the same directory of the file
  if $mirror
    output, _code = server.run("ls -1 #{dir}")
    files = output.split("\n")
    checksum_file = files.find { |file| checksum_file_names.include?(file) }

    raise "SHA256 checksum file not found in #{dir}" unless checksum_file

    "#{dir}/#{checksum_file}"
  # Attempt to download the checksum file
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

      if response.is_a?(Net::HTTPSuccess)
        _output, code = server.run("cd #{dir} && wget --no-check-certificate #{checksum_url}", timeout: 10)
        return "#{dir}/#{name}" if code.zero?
      end
    end

    raise "No SHA256 checksum file to download found for file at #{file_url}"
  end
end

# Computes the SHA256 checksum for the file at the given path and verifies it against a checksum file.
# The original file name is used to retrieve the correct checksum entry
def validate_checksum_with_file(original_file_name, file_path, checksum_path)
  # Search the checksum file for what should be the only non-comment line containing the original file name
  checksum_line, _code = get_target('server').run("grep -v '^#' #{checksum_path} | grep '#{original_file_name}'")
  raise "SHA256 checksum entry for #{original_file_name} not found in #{checksum_path}" unless checksum_line

  # This relies on the fact that SHA256 hashes have a fixed length of 64 hexadecimal characters to extract the checksum
  # and address any issue related to a checksum file having a different internal format compared to the standard
  checksum_match = checksum_line.match(/\b([0-9a-fA-F]{64})\b/)
  raise "SHA256 checksum not found in entry: #{checksum_line}" unless checksum_match

  expected_checksum = checksum_match[1]
  validate_checksum(file_path, expected_checksum)
end

# Computes the SHA256 checksum of the file at the given path and returns a boolean representing whether it
# matches the expected checksum or not
def validate_checksum(file_path, expected_checksum)
  file_checksum, _code = get_target('server').run("sha256sum -b #{file_path} | awk '{print $1}'")
  file_checksum.strip == expected_checksum
end
