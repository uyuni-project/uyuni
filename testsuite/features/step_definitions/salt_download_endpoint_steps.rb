require 'open-uri'
require 'tempfile'

Given(/^I try download "([^"]*)" from channel "([^"]*)"$/) do |rpm, channel|
  url = "#{Capybara.app_host}/rhn/manager/download/#{channel}/getPackage/#{rpm}"
  if @token
    url = "#{url}?#{@token}"
  end
  @download_path = nil
  @download_error = nil
  Tempfile.open(rpm) do |tmpfile|
    @download_path = tmpfile.path
    begin
      open(url, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE) do |urlfile|
        tmpfile.write(urlfile.read)
      end
    rescue OpenURI::HTTPError => e
      @download_error = e
    end
  end
end

Then(/^the download should get a (\d+) response$/) do |code|
  refute_nil(@download_error)
  assert_equal(code.to_i, @download_error.io.status[0].to_i)
end

Then(/^the download should get no error$/) do
  assert_nil(@download_error)
end
