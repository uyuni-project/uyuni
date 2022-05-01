# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "image" namespace
class NamespaceImage
  def initialize(api_test)
    @test = api_test
    @profile = NamespaceImageProfile.new(api_test)
    @store = NamespaceImageStore.new(api_test)
  end

  attr_reader :profile
  attr_reader :store

  def delete(imageid)
    @test.call('image.delete', sessionKey: @test.token, imageId: imageid)
  end

  def get_details(imageid)
    @test.call('image.getDetails', sessionKey: @test.token, imageId: imageid)
  end

  def schedule_image_build(profile_label, version_build, build_hostid, date)
    @test.call('image.scheduleImageBuild', sessionKey: @test.token, profileLabel: profile_label, version: version_build, buildHostId: build_hostid, earliestOccurrence: date)
  end

  def list_images
    @test.call('image.listImages', sessionKey: @test.token)
  end
end

# "image.profile" namespace
class NamespaceImageProfile
  def initialize(api_test)
    @test = api_test
  end

  def create(label, type, store_label, path, actkey)
    @test.call('image.profile.create', sessionKey: @test.token, label: label, type: type, storeLabel: store_label, path: path, activationKey: actkey)
  end

  def delete(label)
    @test.call('image.profile.delete', sessionKey: @test.token, label: label)
  end

  def set_custom_values(label, values)
    @test.call('image.profile.setCustomValues', sessionKey: @test.token, label: label, values: values)
  end

  def delete_custom_values(label, keys)
    @test.call('image.profile.deleteCustomValues', sessionKey: @test.token, label: label, keys: keys)
  end

  def get_custom_values(label)
    @test.call('image.profile.getCustomValues', sessionKey: @test.token, label: label)
  end

  def list_image_profile_types
    @test.call('image.profile.listImageProfileTypes', sessionKey: @test.token)
  end

  def list_image_profiles
    @test.call('image.profile.listImageProfiles', sessionKey: @test.token)
  end

  def get_details(label)
    @test.call('image.profile.getDetails', sessionKey: @test.token, label: label)
  end

  def set_details(label, values)
    @test.call('image.profile.setDetails', sessionKey: @test.token, label: label, details: values)
  end
end

# "image.store" namespace
class NamespaceImageStore
  def initialize(api_test)
    @test = api_test
  end

  def create(label, uri, type, creds = {})
    @test.call('image.store.create', sessionKey: @test.token, label: label, uri: uri, storeType: type, credentials: creds)
  end

  def delete(label)
    @test.call('image.store.delete', sessionKey: @test.token, label: label)
  end

  def list_image_store_types
    @test.call('image.store.listImageStoreTypes', sessionKey: @test.token)
  end

  def list_image_stores
    @test.call('image.store.listImageStores', sessionKey: @test.token)
  end

  def get_details(label)
    @test.call('image.store.getDetails', sessionKey: @test.token, label: label)
  end

  def set_details(label, details)
    @test.call('image.store.setDetails', sessionKey: @test.token, label: label, details: details)
  end
end
