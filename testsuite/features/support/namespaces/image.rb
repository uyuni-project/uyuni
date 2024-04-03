# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

##
# Represents a namespace for managing images.
class NamespaceImage
  ##
  # Initializes a new instance of the NamespaceImage class.
  #
  # @param api_test [Object] The API test object.
  def initialize(api_test)
    @test = api_test
    @profile = NamespaceImageProfile.new(api_test)
    @store = NamespaceImageStore.new(api_test)
  end

  attr_reader :profile, :store

  ##
  # Deletes an image based on its ID.
  #
  # @param imageid [String] The ID of the image you want to delete.
  def delete(imageid)
    @test.call('image.delete', sessionKey: @test.token, imageId: imageid)
  end

  ##
  # Gets an image's details based on its ID.
  #
  # @param imageid [String] The ID of the image you want to get details for.
  def get_details(imageid)
    @test.call('image.getDetails', sessionKey: @test.token, imageId: imageid)
  end

  ##
  # Schedules an image build for a given profile, version, build host, and date.
  #
  # @param profile_label [String] The label of the profile you want to build.
  # @param version_build [String] The version of the image you want to build.
  # @param build_hostid [String] The ID of the build host you want to use.
  # @param date [String] The date and time you want the build to start.
  #   Format: YYYY-MM-DD HH:MM:SS
  def schedule_image_build(profile_label, version_build, build_hostid, date)
    @test.call('image.scheduleImageBuild', sessionKey: @test.token, profileLabel: profile_label, version: version_build, buildHostId: build_hostid, earliestOccurrence: date)
  end

  ##
  # Returns a list of images.
  def list_images
    @test.call('image.listImages', sessionKey: @test.token)
  end
end

# Image Profile namespace
# It's a Ruby class that wraps the API calls for the image.profile namespace
class NamespaceImageProfile
  ##
  # Initializes a new instance of the NamespaceImageProfile class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Create a new image profile.
  #
  # @param label [String] The name of the profile.
  # @param type [String] The type of profile to create.
  # @param store_label [String] The label of the store you want to use.
  # @param path [String] The path to the kickstart file.
  # @param actkey [String] The activation key to use for the profile.
  def create(label, type, store_label, path, actkey)
    @test.call('image.profile.create', sessionKey: @test.token, label: label, type: type, storeLabel: store_label, path: path, activationKey: actkey)
  end

  ##
  # Deletes a profile from the system.
  #
  # @param label [String] The name of the profile to delete.
  def delete(label)
    @test.call('image.profile.delete', sessionKey: @test.token, label: label)
  end

  ##
  # Sets custom values for an image profile, based on its label.
  #
  # @param label [String] The label of the image profile.
  # @param values [Hash] A JSON object containing the custom values to set.
  def set_custom_values(label, values)
    @test.call('image.profile.setCustomValues', sessionKey: @test.token, label: label, values: values)
  end

  ##
  # Delete custom values from an image profile.
  #
  # @param label [String] The label of the image profile you want to delete custom values from.
  # @param keys [Array<String>] The keys of the custom values to delete.
  def delete_custom_values(label, keys)
    @test.call('image.profile.deleteCustomValues', sessionKey: @test.token, label: label, keys: keys)
  end

  ##
  # Returns the custom values for a given label.
  #
  # @param label [String] The label of the image profile you want to get the custom values for.
  def get_custom_values(label)
    @test.call('image.profile.getCustomValues', sessionKey: @test.token, label: label)
  end

  ##
  # Lists the image profile types available in the system.
  def list_image_profile_types
    @test.call('image.profile.listImageProfileTypes', sessionKey: @test.token)
  end

  ##
  # Lists all the image profiles.
  def list_image_profiles
    @test.call('image.profile.listImageProfiles', sessionKey: @test.token)
  end

  ##
  # Returns the details of an image profile based on its label.
  #
  # @param label [String] The label of the image profile you want to get details for.
  def get_details(label)
    @test.call('image.profile.getDetails', sessionKey: @test.token, label: label)
  end

  ##
  # Sets the label and values for an image profile.
  #
  # @param label [String] The label for the details.
  # @param values [Hash] A hash of values to be displayed in the details section.
  def set_details(label, values)
    @test.call('image.profile.setDetails', sessionKey: @test.token, label: label, details: values)
  end
end

##
# The NamespaceImageStore class provides methods for managing image stores in the system.
class NamespaceImageStore
  ##
  # Initializes a new instance of the NamespaceImageStore class.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Creates a new image store.
  #
  # @param label [String] The name of the image store.
  # @param uri [String] The URI path of the image store.
  # @param type [String] The type of the store.
  #   Valid values are: TODO.
  # @param creds [Hash] Credentials.
  def create(label, uri, type, creds = {})
    @test.call('image.store.create', sessionKey: @test.token, label: label, uri: uri, storeType: type, credentials: creds)
  end

  ##
  # Deletes an image store from the system.
  #
  # @param label [String] The name of the image store.
  def delete(label)
    @test.call('image.store.delete', sessionKey: @test.token, label: label)
  end

  ##
  # Lists the image store types available in the system.
  def list_image_store_types
    @test.call('image.store.listImageStoreTypes', sessionKey: @test.token)
  end

  ##
  # Lists the image stores available in the system.
  def list_image_stores
    @test.call('image.store.listImageStores', sessionKey: @test.token)
  end

  ##
  # Gets the details of an image store.
  #
  # @param label [String] The name of the image store.
  def get_details(label)
    @test.call('image.store.getDetails', sessionKey: @test.token, label: label)
  end

  ##
  # Sets the details of an image store.
  #
  # @param label [String] The name of the image store.
  # @param details [Object] Details of the image store.
  def set_details(label, details)
    @test.call('image.store.setDetails', sessionKey: @test.token, label: label, details: details)
  end
end
