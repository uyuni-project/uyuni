# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# Wrapper for HTTP library (currently Faraday)

require 'faraday'

class HttpClient
  def initialize(host)
    @http_client = Faraday.new("https://" + host)
  end

  def call(name, params)

    # Adjust method name
    name.sub!('auth.', '') if name == 'auth.login' || name == 'auth.logout'
    long_name = '/rhn/manager/api/' + name.tr('.', '/')
    short_name = name.split('.')[-1]

    # Get session cookie from previous calls
    if params.nil?
      session_cookie = nil
    else
      session_cookie = params[:sessionKey]
      params.delete(:sessionKey)
    end

    # Call API
    if short_name.start_with?('list', 'get', 'is', 'find') or ['logout', 'errata.applicableToChannels'].include? name
      # GET
      unless params.nil?
        long_name += '?'
        params.each do |key, value|
          long_name += '&' unless long_name[-1] == '?'
          long_name += "#{key}=#{value}"
        end
      end
      answer = @http_client.get(long_name) do |request|
        request.headers['Content-Type'] = 'application/json'
        request.headers['Cookie'] = session_cookie unless session_cookie.nil?
      end
    else
      # POST
      answer = @http_client.post(long_name) do |request|
        request.headers['Content-Type'] = 'application/json'
        request.headers['Cookie'] = session_cookie unless session_cookie.nil?
        request.body = params.to_json unless params.nil?
      end
    end
    raise "Unexpected HTTP status code #{answer.status}" unless answer.status == 200

    # Return either new session cookie or HTTP body
    if name == 'login'
      session_key = ''
      cookies = answer.headers['Set-cookie']
      cookies.split(',').each do |cookie|
        if cookie.include? 'pxt-session-cookie=' and not cookie.include? 'Max-Age=0;'
          session_cookie = cookie.split(';')[0]
        end
      end
      session_cookie
    else
      json_body = JSON.parse(answer.body)
      raise "API failure" unless json_body['success']
      json_body['result']
    end
  end
end
