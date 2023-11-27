# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Image namespace
class NamespaceImage
  def initialize(api_test)
    @test = api_test
    @profile = NamespaceImageProfile.new(api_test)
    @store = NamespaceImageStore.new(api_test)
  end

  attr_reader :profile
  attr_reader :store

  ##
  # Deletes an image based on its ID.
  #
  # Args:
  #   imageid: The ID of the image you want to delete.
  def delete(imageid)
    @test.call('image.delete', sessionKey: @test.token, imageId: imageid)
  end

  ##
  # Gets an image's details based on its ID.
  #
  # Args:
  #   imageid: The ID of the image you want to get details for.
  def get_details(imageid)
    @test.call('image.getDetails', sessionKey: @test.token, imageId: imageid)
  end

  ##
  # This function schedules an image build for a given profile, version, build host, and date.
  #
  # Args:
  #   profile_label: The label of the profile you want to build.
  #   version_build: The version of the image you want to build.
  #   build_hostid: This is the ID of the build host you want to use. You can get this by running the following command:
  #   date: The date and time you want the build to start.  This is in the format of YYYY-MM-DD HH:MM:SS.  For example, if
  # you wanted to start the build at 5:00 PM on January 1, 2015, you would use the following: 2015-01-01 17:00:00.
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
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Create a new image profile.
  #
  # Args:
  #   label: The name of the profile
  #   type: The type of profile to create.  Valid values are: TODO
  #   store_label: The label of the store you want to use.
  #   path: The path to the kickstart file.
  #   actkey: The activation key to use for the profile.
  def create(label, type, store_label, path, actkey)
    @test.call('image.profile.create', sessionKey: @test.token, label: label, type: type, storeLabel: store_label, path: path, activationKey: actkey)
  end

  ##
  # Deletes a profile from the system.
  #
  # Args:
  #   label: The name of the profile to delete.
  def delete(label)
    @test.call('image.profile.delete', sessionKey: @test.token, label: label)
  end

  ##
  # Sets custom values for an image profile, based on its label.
  #
  # Args:
  #   label: The label of the image profile
  #   values: A JSON object containing the custom values to set.
  def set_custom_values(label, values)
    @test.call('image.profile.setCustomValues', sessionKey: @test.token, label: label, values: values)
  end

  ##
  # Delete custom values from an image profile.
  #
  # Args:
  #   label: The label of the image profile you want to delete custom values from.
  #   keys: The keys of the custom values to delete.
  def delete_custom_values(label, keys)
    @test.call('image.profile.deleteCustomValues', sessionKey: @test.token, label: label, keys: keys)
  end

  ##
  # Returns the custom values for a given label.
  #
  # Args:
  #   label: The label of the image profile you want to get the custom values for.
  def get_custom_values(label)
    @test.call('image.profile.getCustomValues', sessionKey: @test.token, label: label)
  end

  ##
  # Lists the image profile types available in the system.
  def list_image_profile_types
    @test.call('image.profile.listImageProfileTypes', sessionKey: @test.token)
  end

  ##
  # It lists all the image profiles.
  def list_image_profiles
    @test.call('image.profile.listImageProfiles', sessionKey: @test.token)
  end

  ##
  # Returns the details of an image profile based on its label.
  #
  # Args:
  #   label: The label of the image profile you want to get details for.
  def get_details(label)
    @test.call('image.profile.getDetails', sessionKey: @test.token, label: label)
  end

  ##
  # Sets the label and values for an image profile.
  #
  # Args:
  #   label: The label for the details.
  #   values: A hash of values to be displayed in the details section.
  def set_details(label, values)
    @test.call('image.profile.setDetails', sessionKey: @test.token, label: label, details: values)
  end
end

# Image Store namespace
class NamespaceImageStore
  ##
  # This function initializes the NamespaceChannelSoftware class.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Creates a new image store.
  #
  # Args:
  #   label: The name of the image store.
  #   uri: The URI path of the image store.
  #   type: The type of the store. Valid values are: TODO.
  #   creds: Credentials.
  def create(label, uri, type, creds = {})
    @test.call('image.store.create', sessionKey: @test.token, label: label, uri: uri, storeType: type, credentials: creds)
  end

  ##
  # Deletes an image store from the system.
  #
  # Args:
  #   label: The name of the image store.
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
  # Args:
  #   label: The name of the image store.
  def get_details(label)
    @test.call('image.store.getDetails', sessionKey: @test.token, label: label)
  end

  ##
  # Sets the details of an image store.
  #
  # Args:
  #   label: The name of the image store.
  #   details: Details of the image store.
  def set_details(label, details)
    @test.call('image.store.setDetails', sessionKey: @test.token, label: label, details: details)
  end
end
