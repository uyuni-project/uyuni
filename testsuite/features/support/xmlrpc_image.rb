require_relative 'xmlrpctest'

# This class enables to use image namespace operations via XML-RPC calls
class XMLRPCImageTest < XMLRPCBaseTest
  def delete_image(imageid)
    @connection.call('image.delete', @sid, imageid)
  end

  def get_image_details(imageid)
    @connection.call('image.get_details', @sid, imageid)
  end

  def schedule_image_build(profile_label, version_build, build_hostid, date)
    @connection.call('image.schedule_image_build', @sid, profile_label, version_build, build_hostid, date)
  end

  def list_images
    @connection.call('image.list_images', @sid)
  end

  # store methods
  def create_store(label, uri, type, params = {})
    @connection.call('image.store.create', @sid, label, uri, type, params)
  end

  def delete_store(label)
    @connection.call('image.store.delete', @sid, label)
  end

  def list_image_store_types
    @connection.call('image.store.list_image_store_types', @sid)
  end

  def list_image_stores
    @connection.call('image.store.list_image_stores', @sid)
  end

  def get_details_store(label)
    @connection.call('image.store.get_details', @sid, label)
  end

  def set_details(label, details)
    @connection.call('image.store.set_details', @sid, label, details)
  end

  # profile methods
  def create_profile(label, type, store_label, path, actkey)
    @connection.call('image.profile.create', @sid, label, type, store_label, path, actkey)
  end

  def delete_profile(label)
    @connection.call('image.profile.delete', @sid, label)
  end

  def set_profile_custom_values(label, values)
    @connection.call('image.profile.set_custom_values', @sid, label, values)
  end

  def delete_profile_custom_values(label, values)
    @connection.call('image.profile.delete_custom_values', @sid, label, values)
  end

  def get_profile_custom_values(label)
    @connection.call('image.profile.get_custom_values', @sid, label)
  end

  def create_custom_key(value, desc)
    @connection.call('system.custominfo.create_key', @sid, value, desc)
  end

  def list_image_profile_types
    @connection.call('image.profile.list_image_profile_types', @sid)
  end

  def list_image_profiles
    @connection.call('image.profile.list_image_profiles', @sid)
  end

  def get_details(label)
    @connection.call('image.profile.get_details', @sid, label)
  end

  def set_profile_details(label, values)
    @connection.call('image.profile.set_details', @sid, label, values)
  end
end
