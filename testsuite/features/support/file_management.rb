# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

# Attempts to retrieve the SHA256 checksum for a file inside a given directory or download it
# from the same domain the file has been downloaded from.
# Returns the path to the checksum or nil if none has been found/downloaded
def get_checksum_path(dir, original_file_name, file_url)
  checksum_file_names = %W[CHECKSUM SHA256SUMS sha256sum.txt #{original_file_name}.CHECKSUM #{original_file_name}.sha256]

  # When using a mirror, the checksum file should be present and in the same directory of the file
  if $mirror
    checksum_file_names.each do |name|
      checksum_path = "#{dir}/#{name}"
      _output, code = get_target('server').run("test -e #{checksum_path}", check_errors: false)
      return checksum_path if code.zero?
    end
  # Attempt to download the checksum file
  else
    base_url = file_url.delete_suffix(original_file_name)

    checksum_file_names.each do |name|
      checksum_url = base_url + name
      _output, code = get_target('server').run("cd #{dir} && wget --no-check-certificate #{checksum_url}", check_errors: false, timeout: 1500)
      return "#{dir}/#{name}" if code.zero?
    end
  end

  # No checksum file found or downloaded
  nil
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
