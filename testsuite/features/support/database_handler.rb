# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.
require 'redis'

# Database handler to interact with a Redis database
class DatabaseHandler
  # Initialize a connection with a Redis database
  # @param redis_host [String] The hostname of the Redis database.
  # @param redis_port [Integer] The port of the Redis database.
  # @param redis_username [String] The username to authenticate with the Redis database.
  # @param redis_password [String] The password to authenticate with the Redis database.
  # @return [Redis] A connection with the Redis database.
  def initialize(redis_host, redis_port, redis_username, redis_password)
    @database = Redis.new(host: redis_host, port: redis_port, username: redis_username, password: redis_password)
  end

  # Close the connection with the Redis database
  def close
    @database.close
  end

  # Add a key-value pair to a Set, optionally selecting a database
  #
  # @param key [String] The key to add the value to.
  # @param value [String] The value to add to the key.
  # @param database [Integer] The database to select (default: 0).
  # @return [String] `OK`
  def add(key, value, database = 0)
    begin
      @database.select(database)
      @database.sadd(key, value)
    rescue StandardError => e
      warn("#{e.backtrace} > #{key} : #{value}")
    end
  end
end
