# Copyright (c) 2017 SUSE Linux
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'time'
require 'date'
require 'securerandom'
require 'timeout'

# this module test image_profile
module ImageProfile
  def create_delete
    cont_op = XMLRPCImageTest.new(ENV['SERVER'])
    cont_op.login('admin', 'admin')
    # create delete profile test
    cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
    cont_op.delete_profile('fakeone')
    cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '1-DOCKER-TEST')
    cont_op.delete_profile('fakeone')
  end

  def custom_values
    cont_op = XMLRPCImageTest.new(ENV['SERVER'])
    cont_op.login('admin', 'admin')
    cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
    cont_op.create_custom_key('arancio', 'test containers')
    values = {}
    values['arancio'] = 'arancia xmlrpc tests'
    cont_op.set_profile_custom_values('fakeone', values)
    pro_det = cont_op.get_profile_custom_values('fakeone')
    assert_equal(pro_det['arancio'], 'arancia xmlrpc tests', 'setting custom profile value failed')
    pro_type = cont_op.list_image_profile_types
    assert_equal(pro_type.length, 1, 'support at moment only one type of Profile!')
    assert_equal(pro_type[0], 'dockerfile', 'type is not dockerfile?')
    key = ['arancio']
    cont_op.delete_profile_custom_values('fakeone', key)
  end

  def image_profiles_xmlrpc
    cont_op = XMLRPCImageTest.new(ENV['SERVER'])
    cont_op.login('admin', 'admin')
    # create delete tests
    create_delete
    # set get delete Custom Values
    custom_values
    puts cont_op.list_image_profiles
    # test listImageProfiles method
    ima_profiles = cont_op.list_image_profiles
    imagelabel = ima_profiles.select { |image| image['label'] = 'fakeone' }
    assert_equal(imagelabel[0]['label'], 'fakeone', 'label of container should be fakeone!')
    # test set value and get value call
    details = {}
    details['storeLabel'] = 'galaxy-registry'
    details['path'] = 'TestForFun'
    details['activationKey'] = ''
    cont_op.set_profile_details('fakeone', details)
    cont_detail = cont_op.get_details('fakeone')
    assert_equal(cont_detail['label'], 'fakeone', 'label test fail!')
    assert_equal(cont_detail['imageType'], 'dockerfile', 'imagetype test fail!')
    cont_op.delete_profile('fakeone')
  end
end

World(ImageProfile)

# container_operations
cont_op = XMLRPCImageTest.new(ENV['SERVER'])
# retrieve minion id, needed for scheduleImageBuild call
def retrieve_minion_id
  sysrpc = XMLRPCSystemTest.new(ENV['SERVER'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.list_systems
  refute_nil(systems)
  minion_id = systems
              .select { |s| s['name'] == $minion.full_hostname }
              .map { |s| s['id'] }.first
  refute_nil(minion_id, "Minion #{$minion.full_hostname} is not yet registered?")
  minion_id
end

And(/^I select sle-minion hostname in Build Host$/) do
  select($minion.full_hostname, from: 'buildHostId')
end

And(/^I navigate to images webpage$/) do
  visit("https://#{$server.full_hostname}/rhn/manager/cm/images")
end

And(/^I navigate to images build webpage$/) do
  visit("https://#{$server.full_hostname}/rhn/manager/cm/build")
end

And(/^container "([^"]*)" built successfully$/) do |name|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  image_id = 0
  images_list.each do |element|
    if element['name'] == name
      image_id = element['id']
      break
    end
  end
  if image_id == 0
    raise 'unable to find the image id'
  end
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        idetails = cont_op.get_image_details(image_id)
        if idetails['buildStatus'] == 'completed'
          break
        elsif idetails['buildStatus'] == 'failed'
          raise "image build failed."
        end
        sleep 5
      end
    end
  rescue Timeout::Error
    raise "image build failed. Timeout"
  end
end

And(/^all "([^"]*)" container images should be built correctly in the GUI$/) do |count|
  def ck_container_imgs(count)
    Timeout.timeout(DEFAULT_TIMEOUT) do
      raise 'error detected while building images' if has_xpath?("//*[contains(@title, 'Failed')]")
      break if has_xpath?("//*[contains(@title, 'Built')]", count: count)
      sleep 5
      step %(I navigate to images webpage)
    end
  rescue Timeout::Error
    raise 'at least one image was not built correctly'
  end
  # don't run this for sles11 (docker feature is not there)
  ck_container_imgs(count) unless sle11family($minion)
end

And(/^I schedule the build of image "([^"]*)" via XML-RPC calls$/) do |image|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = ''
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_hostid, date_build)
end

And(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image, version|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = version
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_hostid, date_build)
end

And(/^I delete the image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image_name_todel, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  refute_nil(images_list, 'ERROR: no images at all were retrieved.')
  images_list.each do |element|
    if element['name'] == image_name_todel.strip && element['version'] == version.strip
      $image_id = element['id']
    end
  end
  cont_op.delete_image($image_id)
end

And(/^the image "([^"]*)" with version "([^"]*)" doesn't exist via XML-RPC calls$/) do |image_non_exist, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  images_list.each do |element|
    raise "#{image_non_exist} should not exist anymore" if element['name'] == image_non_exist && element['version'] == version.strip
  end
end

# images stores tests
And(/^I run image.store tests via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  # Test create and delete calls
  # create and delete a store, even with invalid uri.
  cont_op.create_store('fake_store', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  cont_op.delete_store('fake_store')
  # test list images and list image types call
  store_typ = cont_op.list_image_store_types
  assert_equal(store_typ.length, 1, 'we have only type support for Registry! New method added?! please update the tests')
  assert_equal(store_typ[0]['label'], 'registry', 'imagestore label type should be registry!')
  registry_list = cont_op.list_image_stores
  # print just for debug
  puts registry_list
  assert_equal(registry_list[0]['label'], 'galaxy-registry', 'label is galaxy!')
  assert_equal(registry_list[0]['uri'], 'registry.mgr.suse.de', 'uri should be registry.mgr.suse.de')
  # test setDetails call
  # delete if test fail in the middle. delete image doesn't raise an error if image doesn't exists
  cont_op.create_store('Norimberga', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  details_store = {}
  details_store['uri'] = 'Germania'
  details_store['username'] = ''
  details_store['password'] = ''
  cont_op.set_details('Norimberga', details_store)
  # test get_details call
  details = cont_op.get_details_store('Norimberga')
  assert_equal(details['uri'], 'Germania', 'uri should be Germania')
  assert_equal(details['username'], '', 'username should be empty')
  cont_op.delete_store('Norimberga')
end

And(/^I create "([^"]*)" random image stores$/) do |count|
  cont_op.login('admin', 'admin')
  $labels = []
  count.to_i.times do
    label = SecureRandom.urlsafe_base64(10)
    $labels.push(label)
    uri = SecureRandom.urlsafe_base64(13)
    cont_op.create_store(label, uri, 'registry')
  end
end

And(/^I delete the random image stores$/) do
  cont_op.login('admin', 'admin')
  $labels.each do |label|
    cont_op.delete_store(label)
  end
end

# Profiles tests using module
And(/^I run image.profiles tests via XML-RPC$/, :image_profiles_xmlrpc)

Then(/I create "([^"]*)" random "([^"]*)" containers$/) do |count, image_input|
  cont_op.login('admin', 'admin')
  image = image_input
  build_hostid = retrieve_minion_id
  count.to_i.times do
    version_build = SecureRandom.urlsafe_base64(10)
    now = DateTime.now
    date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
    cont_op.schedule_image_build(image, version_build, build_hostid, date_build)
  end
end
