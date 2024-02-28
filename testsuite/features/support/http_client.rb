# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'faraday'

# When we pass a list of values in the query string of an HTTP request, the defaul encoder will only update the key for that param
# As an example: sids=1000010027&sids=1000010010&sids=1000010012 maps to params={"sids"=>"1000010012"}
# With FlatParamEncoder, it maps to params={"sids"=>["1000010027", "1000010010", "1000010012"]}
Faraday::Utils.default_params_encoder = Faraday::FlatParamsEncoder

# Wrapper class for HTTP client library (Faraday)
class HttpClient
  ##
  #
  # Creates a new HTTP client using the Faraday library.
  #
  # Args:
  #   host: The hostname of the server you want to connect to.
  def initialize(host, ssl_verify = true)
    puts 'Activating HTTP API'
    @http_client = Faraday.new("https://#{host}", request: { timeout: DEFAULT_TIMEOUT }, ssl: { verify: ssl_verify })
  end

  ##
  # It takes a name of a Spacewalk API call and a hash of parameters and returns a tuple of the HTTP method and the URL to
  # call.
  #
  # Args:
  #   name: The name of the API call.
  #   params: A hash of parameters to pass to the API call.
  def prepare_call(name, params)
    short_name = name.split('.')[-1]
    call_type =
      if short_name.start_with?('list', 'get', 'is', 'find') || name.start_with?('system.search.', 'packages.search.') || ['errata.applicableToChannels'].include?(name)

        'GET'
      else
        'POST'
      end
    url = "/rhn/manager/api/#{name.tr('.', '/')}"
    if call_type == 'GET'
      url += '?'
      unless params.nil?
        params.each do |key, value|
          url += '&' unless url[-1] == '?'
          url +=
            if value.is_a?(Array)
              value.map { |v| "#{key}=#{v}" }.join('&')
            else
              "#{key}=#{value}"
            end
        end
      end
    end
    [call_type, url]
  end

  ##
  # It takes a name and a hash of parameters, calls the API, and returns the result.
  #
  # Args:
  #   name: The name of the API call, e.g. 'auth.login'
  #   params: A hash of parameters to pass to the API call.
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
      raise ScriptError, "Unexpected HTTP status code #{answer.status}" if answer.body.empty?

      json_body = JSON.parse(answer.body)
      raise ScriptError, "Unexpected HTTP status code #{answer.status}, message: #{json_body['message']}"
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
      raise SystemCallError, "API failure: #{json_body['message']}" unless json_body['success']

      json_body['result']
    end
  end
end
