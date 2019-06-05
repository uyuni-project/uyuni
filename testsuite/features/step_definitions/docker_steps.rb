# Copyright (c) 2017 SUSE Linux
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'time'
require 'date'
require 'timeout'

# container operations
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

When(/^I select sle-minion hostname in Build Host$/) do
  select($minion.full_hostname, from: 'buildHostId')
end

When(/^I navigate to images webpage$/) do
  visit("https://#{$server.full_hostname}/rhn/manager/cm/images")
end

When(/^I navigate to images build webpage$/) do
  visit("https://#{$server.full_hostname}/rhn/manager/cm/build")
end

When(/^I wait at most (\d+) seconds until container "([^"]*)" is built successfully$/) do |timeout, name|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  image_id = 0
  images_list.each do |element|
    if element['name'] == name
      image_id = element['id']
      break
    end
  end
  raise 'unable to find the image id' if image_id.zero?

  repeat_until_timeout(timeout: timeout.to_i, message: 'image build did not complete') do
    idetails = cont_op.get_image_details(image_id)
    break if idetails['buildStatus'] == 'completed' && idetails['inspectStatus'] == 'completed'
    raise 'image build failed.' if idetails['buildStatus'] == 'failed'
    raise 'image inspect failed.' if idetails['inspectStatus'] == 'failed'
    sleep 5
  end
end

When(/^I wait at most (\d+) seconds until all "([^"]*)" container images are built correctly in the GUI$/) do |timeout, count|
  def ck_container_imgs(timeout, count)
    repeat_until_timeout(timeout: timeout.to_i, message: 'at least one image was not built correctly') do
      step %(I navigate to images webpage)
      step %(I wait until I do not see "There are no entries to show." text)
      raise 'error detected while building images' if has_xpath?("//*[contains(@title, 'Failed')]")
      break if has_xpath?("//*[contains(@title, 'Built')]", count: count)
      sleep 5
    end
  end
  # don't run this for sles11 (docker feature is not there)
  ck_container_imgs(timeout, count) unless sle11family($minion)
end

When(/^I check the first image$/) do
  within(:xpath, '//section') do
    row = first(:xpath, "//div[@class='table-responsive']/table/tbody/tr[.//td]")
    row.first(:xpath, ".//input[@type='checkbox']").set(true)
  end
end

When(/^I schedule the build of image "([^"]*)" via XML-RPC calls$/) do |image|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = ''
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_hostid, date_build)
end

When(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image, version|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = version
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_hostid, date_build)
end

When(/^I delete the image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image_name_todel, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  refute_nil(images_list, 'ERROR: no images at all were retrieved.')
  image_id = 0
  images_list.each do |element|
    if element['name'] == image_name_todel.strip && element['version'] == version.strip
      image_id = element['id']
    end
  end
  if image_id.zero?
    puts "Image #{image_name_todel} with version #{version} does not exist, skipping"
  else
    cont_op.delete_image(image_id)
  end
end

Then(/^the image "([^"]*)" with version "([^"]*)" doesn't exist via XML-RPC calls$/) do |image_non_exist, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.list_images
  images_list.each do |element|
    raise "#{image_non_exist} should not exist anymore" if element['name'] == image_non_exist && element['version'] == version.strip
  end
end

# image stores tests
When(/^I create and delete an image store via XML-RPC$/) do
  # create and delete a store, even with invalid URI
  cont_op.login('admin', 'admin')
  cont_op.create_store('fake_store', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  cont_op.delete_store('fake_store')
end

When(/^I list image store types and image stores via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  store_typ = cont_op.list_image_store_types
  assert_equal(store_typ.length, 2, 'we have only type support for Registry and OS Image Store type! New method added?! please update the tests')
  assert_equal(store_typ[0]['label'], 'registry', 'imagestore label type should be registry!')
  assert_equal(store_typ[1]['label'], 'os_image', 'imagestore label type should be OS image!')
  registry_list = cont_op.list_image_stores
  puts registry_list
  assert_equal(registry_list[0]['label'], 'galaxy-registry', 'label is galaxy!')
  assert_equal(registry_list[0]['uri'], 'registry.mgr.suse.de', 'uri should be registry.mgr.suse.de')
end

When(/^I set and get details of image store via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  # test setDetails call
  # delete if test fail in the middle. delete image doesn't raise an error if image doesn't exists
  cont_op.create_store('Norimberga', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  details_store = {}
  details_store['uri'] = 'Germania'
  details_store['username'] = ''
  details_store['password'] = ''
  cont_op.set_details('Norimberga', details_store)
  # test getDetails call
  details = cont_op.get_details_store('Norimberga')
  assert_equal(details['uri'], 'Germania', 'uri should be Germania')
  assert_equal(details['username'], '', 'username should be empty')
  cont_op.delete_store('Norimberga')
end

# profiles tests
When(/^I create and delete profiles via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  cont_op.delete_profile('fakeone')
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '1-DOCKER-TEST')
  cont_op.delete_profile('fakeone')
end

When(/^I create and delete profile custom values via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  cont_op.create_custom_key('arancio', 'test containers')
  values = {}
  values['arancio'] = 'arancia xmlrpc tests'
  cont_op.set_profile_custom_values('fakeone', values)
  pro_det = cont_op.get_profile_custom_values('fakeone')
  assert_equal(pro_det['arancio'], 'arancia xmlrpc tests', 'setting custom profile value failed')
  pro_type = cont_op.list_image_profile_types
  assert_equal(pro_type.length, 2, 'support for Dockerfile and Kiwi profiles')
  assert_equal(pro_type[0], 'dockerfile', 'type is not dockerfile?')
  assert_equal(pro_type[1], 'kiwi', 'type is not kiwi?')
  key = ['arancio']
  cont_op.delete_profile_custom_values('fakeone', key)
end

When(/^I list image profiles via XML-RPC$/) do
  cont_op.login('admin', 'admin')
  puts cont_op.list_image_profiles
  ima_profiles = cont_op.list_image_profiles
  imagelabel = ima_profiles.select { |image| image['label'] = 'fakeone' }
  assert_equal(imagelabel[0]['label'], 'fakeone', 'label of container should be fakeone!')
end

When(/^I set and get profile details via XML-RPC$/) do
  cont_op.login('admin', 'admin')
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
