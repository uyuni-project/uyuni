# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'faraday'

# Wrapper class for HTTP client library (Faraday)
class HttpClient
  def initialize(host)
    puts 'Activating HTTP API'
    @http_client = Faraday.new('https://' + host, request: { timeout: DEFAULT_TIMEOUT })
  end

  def prepare_call(name, params)
    short_name = name.split('.')[-1]
    call_type =
      if short_name.start_with?('list', 'get', 'is', 'find') ||
         name.start_with?('system.search.', 'packages.search.') ||
         ['auth.logout', 'errata.applicableToChannels'].include?(name)
        'GET'
      else
        'POST'
      end
    url = '/rhn/manager/api/' + name.tr('.', '/')
    if call_type == 'GET'
      url += '?'
      unless params.nil?
        params.each do |key, value|
          url += '&' unless url[-1] == '?'
          url += "#{key}=#{value}"
        end
      end
    end
    [call_type, url]
  end

  def call(name, params)
    # Get session cookie from previous calls
    if params.nil?
      session_cookie = nil
    else
      session_cookie = params[:sessionKey]
      params.delete(:sessionKey)
    end

    # Call API
    call_type, url = prepare_call(name, params)
    answer =
      if call_type == 'GET'
        @http_client.get(url) do |request|
          request.headers['Content-Type'] = 'application/json'
          request.headers['Cookie'] = session_cookie unless session_cookie.nil?
        end
      else
        @http_client.post(url) do |request|
          request.headers['Content-Type'] = 'application/json'
          request.headers['Cookie'] = session_cookie unless session_cookie.nil?
          request.body = params.to_json unless params.nil?
        end
      end
    unless answer.status == 200
      raise "Unexpected HTTP status code #{answer.status}" if answer.body.empty?

      json_body = JSON.parse(answer.body)
      raise "Unexpected HTTP status code #{answer.status}, message: #{json_body['message']}"
    end

    # Return either new session cookie or HTTP body
    if name == 'auth.login'
      session_cookie = ''
      cookies = answer.headers['Set-cookie']
      cookies.split(',').each do |cookie|
        # isolate the new session cookie, but ignore the expired one (with Max-Age=0)
        if cookie.include?('pxt-session-cookie=') && !cookie.include?('Max-Age=0;')
          session_cookie = cookie.split(';')[0]
        end
      end
      session_cookie
    else
      json_body = JSON.parse(answer.body)
      raise "API failure: #{json_body['message']}" unless json_body['success']

      json_body['result']
    end
  end
end
