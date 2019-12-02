# Copyright (c) 2017-2020 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'time'
require 'date'
require 'timeout'

# container operations
cont_op = XMLRPCImageTest.new(ENV['SERVER'])

# retrieve build host id, needed for scheduleImageBuild call
def retrieve_build_host_id
  sysrpc = XMLRPCSystemTest.new(ENV['SERVER'])
  sysrpc.login($username, $password)
  systems = sysrpc.list_systems
  refute_nil(systems)
  build_host_id = systems
                  .select { |s| s['name'] == $build_host.full_hostname }
                  .map { |s| s['id'] }.first
  refute_nil(build_host_id, "Build host #{$build_host.full_hostname} is not yet registered?")
  build_host_id
end

# OS image build
Then(/^I wait until the image build "([^"]*)" is completed$/) do |image_name|
  steps %(
    When I wait at most 3300 seconds until event "Image Build #{image_name} scheduled by kiwikiwi" is completed
    And I wait at most 300 seconds until event "Image Inspect 1//#{image_name}:latest scheduled by kiwikiwi" is completed
  )
end

Then(/^I am on the image store of the kiwi image for organization "([^"]*)"$/) do |org|
  step %(I navigate to "os-images/#{org}/" page)
end

Then(/^I should see the name of the image$/) do
  step %(I should see a "#{compute_image_name}" text)
end

When(/^I wait at most (\d+) seconds until container "([^"]*)" is built successfully$/) do |timeout, name|
  cont_op.login($username, $password)
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
      step %(I follow the left menu "Images > Image List")
      step %(I wait until I do not see "There are no entries to show." text)
      raise 'error detected while building images' if all(:xpath, "//*[contains(@title, 'Failed')]").any?
      break if has_xpath?("//*[contains(@title, 'Built')]", count: count)
      sleep 5
    end
  end
  # don't run this for sles11 (docker feature is not there)
  ck_container_imgs(timeout, count) unless sle11family?($build_host)
end

When(/^I check the first image$/) do
  step %(I check the first row in the list)
end

When(/^I schedule the build of image "([^"]*)" via XML-RPC calls$/) do |image|
  cont_op.login($username, $password)
  # empty by default
  version_build = ''
  build_host_id = retrieve_build_host_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_host_id, date_build)
end

When(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image, version|
  cont_op.login($username, $password)
  # empty by default
  version_build = version
  build_host_id = retrieve_build_host_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.schedule_image_build(image, version_build, build_host_id, date_build)
end

When(/^I delete the image "([^"]*)" with version "([^"]*)" via XML-RPC calls$/) do |image_name_todel, version|
  cont_op.login($username, $password)
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
  cont_op.login($username, $password)
  images_list = cont_op.list_images
  images_list.each do |element|
    raise "#{image_non_exist} should not exist anymore" if element['name'] == image_non_exist && element['version'] == version.strip
  end
end

# image stores tests
When(/^I create and delete an image store via XML-RPC$/) do
  # create and delete a store, even with invalid URI
  cont_op.login($username, $password)
  cont_op.create_store('fake_store', 'https://github.com/uyuni-project/uyuni', 'registry')
  cont_op.delete_store('fake_store')
end

When(/^I list image store types and image stores via XML-RPC$/) do
  cont_op.login($username, $password)
  store_typ = cont_op.list_image_store_types
  raise 'We have only type support for Registry and OS Image Store type! New method added?! please update the tests' unless store_typ.length == 2
  raise "imagestore label type should be 'registry' but is #{store_typ[0]['label']}" unless store_typ[0]['label'] == 'registry'
  raise "imagestore label type should be 'os_image' but is #{store_typ[1]['label']}" unless store_typ[1]['label'] == 'os_image'

  registry_list = cont_op.list_image_stores
  raise "Label #{registry_list[0]['label']} is different than 'galaxy-registry'" unless registry_list[0]['label'] == 'galaxy-registry'
  raise "URI #{registry_list[0]['uri']} is different than '#{$no_auth_registry}'" unless registry_list[0]['uri'] == $no_auth_registry.to_s
end

When(/^I set and get details of image store via XML-RPC$/) do
  cont_op.login($username, $password)
  # test setDetails call
  # delete if test fail in the middle. delete image doesn't raise an error if image doesn't exists
  cont_op.create_store('Norimberga', 'https://github.com/uyuni-project/uyuni', 'registry')
  details_store = {}
  details_store['uri'] = 'Germania'
  details_store['username'] = ''
  details_store['password'] = ''
  cont_op.set_details('Norimberga', details_store)
  # test getDetails call
  details = cont_op.get_details_store('Norimberga')
  raise "uri should be Germania but is #{details['uri']}" unless details['uri'] == 'Germania'
  raise "username should be empty but is #{details['username']}" unless details['username'] == ''
  cont_op.delete_store('Norimberga')
end

# profiles tests
When(/^I create and delete profiles via XML-RPC$/) do
  cont_op.login($username, $password)
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  cont_op.delete_profile('fakeone')
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '1-DOCKER-TEST')
  cont_op.delete_profile('fakeone')
end

When(/^I create and delete profile custom values via XML-RPC$/) do
  cont_op.login($username, $password)
  cont_op.create_profile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  cont_op.create_custom_key('arancio', 'test containers')
  values = {}
  values['arancio'] = 'arancia xmlrpc tests'
  cont_op.set_profile_custom_values('fakeone', values)
  pro_det = cont_op.get_profile_custom_values('fakeone')
  raise "setting custom profile value failed: #{pro_det['arancio']} != 'arancia xmlrpc tests'" unless pro_det['arancio'] == 'arancia xmlrpc tests'
  pro_type = cont_op.list_image_profile_types
  raise "Number of image profile types is #{pro_type.length}" unless pro_type.length == 2
  raise "type #{pro_type[0]} is not dockerfile" unless pro_type[0] == 'dockerfile'
  raise "type #{pro_type[1]} is not kiwi" unless pro_type[1] == 'kiwi'
  key = ['arancio']
  cont_op.delete_profile_custom_values('fakeone', key)
end

When(/^I list image profiles via XML-RPC$/) do
  cont_op.login($username, $password)
  puts cont_op.list_image_profiles
  ima_profiles = cont_op.list_image_profiles
  imagelabel = ima_profiles.select { |image| image['label'] = 'fakeone' }
  raise "label of container should be fakeone! #{imagelabel[0]['label']} != 'fakeone'" unless imagelabel[0]['label'] == 'fakeone'
end

When(/^I set and get profile details via XML-RPC$/) do
  cont_op.login($username, $password)
  details = {}
  details['storeLabel'] = 'galaxy-registry'
  details['path'] = 'TestForFun'
  details['activationKey'] = ''
  cont_op.set_profile_details('fakeone', details)
  cont_detail = cont_op.get_details('fakeone')
  raise "label test fail! #{cont_detail['label']} != 'fakeone'" unless cont_detail['label'] == 'fakeone'
  raise "imagetype test fail! #{cont_detail['imageType']} != 'dockerfile'" unless cont_detail['imageType'] == 'dockerfile'
  cont_op.delete_profile('fakeone')
end
