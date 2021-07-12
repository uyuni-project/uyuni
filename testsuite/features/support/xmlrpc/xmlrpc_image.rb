# Copyright (c) 2017-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Image API Namespace
class XMLRPCImageTest < XMLRPCBaseTest
  # Delete a image
  def delete_image(imageid)
    @connection.call('image.delete', @sid, imageid)
  end

  # Get image details
  def image_details(imageid)
    @connection.call('image.details', @sid, imageid)
  end

  # Schedule image build
  def schedule_image_build(profile_label, version_build, build_hostid, date)
    @connection.call('image.schedule_image_build', @sid, profile_label, version_build, build_hostid, date)
  end

  # Get the list of images
  def list_images
    @connection.call('image.list_images', @sid)
  end

  # store methods
  def create_store(label, uri, type, params: {})
    @connection.call('image.store.create', @sid, label, uri, type, params)
  end

  # Delete a store by label
  def delete_store(label)
    @connection.call('image.store.delete', @sid, label)
  end

  # Get a list images store types
  def list_image_store_types
    @connection.call('image.store.list_image_store_types', @sid)
  end

  # Get list image stores
  def list_image_stores
    @connection.call('image.store.list_image_stores', @sid)
  end

  # Get details of a image store
  def details_store(label)
    @connection.call('image.store.details', @sid, label)
  end

  # Set details of a image store
  def set_details(label, details)
    @connection.call('image.store.set_details', @sid, label, details)
  end

  # Create a image profile
  def create_profile(label, type, store_label, path, actkey)
    @connection.call('image.profile.create', @sid, label, type, store_label, path, actkey)
  end

  # Delete a image profile by label
  def delete_profile(label)
    @connection.call('image.profile.delete', @sid, label)
  end

  # Set custom values in a image profile
  def set_profile_custom_values(label, values)
    @connection.call('image.profile.set_custom_values', @sid, label, values)
  end

  # delete custom values in a image profile
  def delete_profile_custom_values(label, values)
    @connection.call('image.profile.delete_custom_values', @sid, label, values)
  end

  # Get cutom values of a image profile
  def profile_custom_values(label)
    @connection.call('image.profile.get_custom_values', @sid, label)
  end

  # Get a list of image profile types
  def list_image_profile_types
    @connection.call('image.profile.list_image_profile_types', @sid)
  end

  # Get a list of image profiles
  def list_image_profiles
    @connection.call('image.profile.list_image_profiles', @sid)
  end

  # Get image profile details
  def details(label)
    @connection.call('image.profile.details', @sid, label)
  end

  # Set image profile details
  def set_profile_details(label, values)
    @connection.call('image.profile.set_details', @sid, label, values)
  end
end
