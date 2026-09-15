# Copyright (c) 2024 SUSE LLC.
# SPDX-License-Identifier: MIT
require 'redis'

# Key-Value Store to interact with a NoSQL database
class KeyValueStore
  # Initialize a connection with a NoSQL database
  #
  # @param db_host [String] The hostname of the NoSQL database.
  # @param db_port [Integer] The port of the NoSQL database.
  # @param db_username [String] The username to authenticate with the NoSQL database.
  # @param db_password [String] The password to authenticate with the NoSQL database.
  # @return [Redis] A connection with the database.
  def initialize(db_host, db_port, db_username, db_password)
    begin
      raise ArgumentError, 'Database host is required' if db_host.nil? || db_host.empty?
      raise ArgumentError, 'Database port is required' if db_port.nil?
      raise ArgumentError, 'Database username is required' if db_username.nil? || db_username.empty?
      raise ArgumentError, 'Database password is required' if db_password.nil? || db_password.empty?

      @database = Redis.new(host: db_host, port: db_port, username: db_username, password: db_password)
    rescue StandardError => e
      warn("Error initializing KeyValueStore:\n #{e.full_message}")
      raise
    end
  end

  # Close the connection with the NoSQL database
  def close
    @database.close
  end

  # Add a key-value pair to a Set
  #
  # @param key [String] The key to add the value to.
  # @param value [String] The value to add to the key.
  # @param database [Integer] Optional: The database number to select (default: 0).
  def add(key, value, database = 0)
    begin
      @database.select(database)
      @database.sadd(key, value)
    rescue StandardError => e
      warn("Error adding a key-value:\n #{e.full_message}")
      raise
    end
  end

  # Get the value of a key
  #
  # @param key [String] The key to get the value from.
  # @param database [Integer] Optional: The database number to select (default: 0).
  # @return [Array<String>] An array with the values of the key.
  def get(key, database = 0)
    begin
      @database.select(database)
      @database.smembers(key)
    rescue StandardError => e
      warn("Error getting a key-value:\n #{e.full_message}")
      raise
    end
  end

  # Remove a key-value pair from a Set
  #
  # @param key [String] The key to remove the value from.
  # @param value [String] The value to remove from the key.
  # @param database [Integer] Optional: The database number to select (default: 0).
  # @return [Integer] The number of members that were successfully removed
  def remove(key, value, database = 0)
    begin
      @database.select(database)
      @database.srem(key, value)
    rescue StandardError => e
      warn("Error removing a key-value:\n #{e.full_message}")
      raise
    end
  end
end
