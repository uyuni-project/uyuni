# Copyright (c) 2017-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning Docker and containerization.

require 'time'
require 'date'
require 'timeout'

When(/^I enter "([^"]*)" relative to profiles as "([^"]*)"$/) do |path, field|
  git_profiles = ENV['GITPROFILES']
  step %(I enter "#{git_profiles}/#{path}" as "#{field}")
end

When(/^I enter URI, username and password for registry$/) do
  auth_registry_username, auth_registry_password = ENV['AUTH_REGISTRY_CREDENTIALS'].split('|')
  steps %(
    When I enter "#{$auth_registry}" as "uri"
    And I enter "#{auth_registry_username}" as "username"
    And I enter "#{auth_registry_password}" as "password"
  )
end

When(/^I wait at most (\d+) seconds until image "([^"]*)" with version "([^"]*)" is built successfully via API$/) do |timeout, name, version|
  images_list = $api_test.image.list_images
  log "List of images: #{images_list}"
  image_id = 0
  images_list.each do |element|
    if element['name'] == name && element['version'] == version
      image_id = element['id']
      break
    end
  end
  raise 'unable to find the image id' if image_id.zero?

  repeat_until_timeout(timeout: timeout.to_i, message: 'image build did not complete') do
    image_details = $api_test.image.get_details(image_id)
    log "Image Details: #{image_details}"
    break if image_details['buildStatus'] == 'completed'
    raise 'image build failed.' if image_details['buildStatus'] == 'failed'
    sleep 5
  end
end

When(/^I wait at most (\d+) seconds until image "([^"]*)" with version "([^"]*)" is inspected successfully via API$/) do |timeout, name, version|
  images_list = $api_test.image.list_images
  log "List of images: #{images_list}"
  image_id = 0
  images_list.each do |element|
    if element['name'] == name && element['version'] == version
      image_id = element['id']
      break
    end
  end
  raise 'unable to find the image id' if image_id.zero?

  repeat_until_timeout(timeout: timeout.to_i, message: 'image inspection did not complete') do
    image_details = $api_test.image.get_details(image_id)
    log "Image Details: #{image_details}"
    break if image_details['inspectStatus'] == 'completed'
    raise 'image inspect failed.' if image_details['inspectStatus'] == 'failed'
    sleep 5
  end
end

# Warning: this can be confused by failures in previous scenarios
# so it should be used only in the first image building scenario
When(/^I wait at most (\d+) seconds until all "([^"]*)" container images are built correctly on the Image List page$/) do |timeout, count|
  repeat_until_timeout(timeout: timeout.to_i, message: 'at least one image was not built correctly') do
    step 'I follow the left menu "Images > Image List"'
    step 'I wait until I do not see "There are no entries to show." text'
    raise 'error detected while building images' if has_xpath?('//tr[td[text()=\'Container Image\']][td//*[contains(@title, \'Failed\')]]')
    break if has_xpath?('//tr[td[text()=\'Container Image\']][td//*[contains(@title, \'Built\')]]', count: count)
    sleep 5
  end
end

When(/^I schedule the build of image "([^"]*)" via API calls$/) do |image|
  # empty by default
  version_build = ''
  build_host_id = retrieve_build_host_id
  date_build = $api_test.date_now
  $api_test.image.schedule_image_build(image, version_build, build_host_id, date_build)
end

When(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via API calls$/) do |image, version|
  # empty by default
  version_build = version
  build_host_id = retrieve_build_host_id
  date_build = $api_test.date_now
  $api_test.image.schedule_image_build(image, version_build, build_host_id, date_build)
end

When(/^I delete the image "([^"]*)" with version "([^"]*)" via API calls$/) do |image_name_todel, version|
  images_list = $api_test.image.list_images
  refute_nil(images_list, 'ERROR: no images at all were retrieved.')
  image_id = 0
  images_list.each do |element|
    if element['name'] == image_name_todel.strip && element['version'] == version.strip
      image_id = element['id']
    end
  end
  if image_id.zero?
    log "Image #{image_name_todel} with version #{version} does not exist, skipping"
  else
    $api_test.image.delete(image_id)
  end
end

Then(/^the list of packages of image "([^"]*)" with version "([^"]*)" is not empty$/) do |name, version|
  images_list = $api_test.image.list_images
  log "List of images: #{images_list}"
  image_id = 0
  images_list.each do |element|
    if element['name'] == name && element['version'] == version
      image_id = element['id']
      break
    end
  end
  raise 'unable to find the image id' if image_id.zero?

  image_details = $api_test.image.get_details(image_id)
  log "Image Details: #{image_details}"
  raise 'the list of image packages is empty' if (image_details['installedPackages']).zero?
end

Then(/^the image "([^"]*)" with version "([^"]*)" doesn't exist via API calls$/) do |image_non_exist, version|
  images_list = $api_test.image.list_images
  images_list.each do |element|
    raise "#{image_non_exist} should not exist anymore" if element['name'] == image_non_exist && element['version'] == version.strip
  end
end

# image stores tests
When(/^I create and delete an image store via API$/) do
  # create and delete a store, even with invalid URI
  $api_test.image.store.create('fake_store', 'https://github.com/uyuni-project/uyuni', 'registry')
  $api_test.image.store.delete('fake_store')
end

When(/^I list image store types and image stores via API$/) do
  store_types = $api_test.image.store.list_image_store_types
  log "Store types: #{store_types}"
  raise 'We have only type support for Registry and OS Image store type! New method added?! please update the tests' unless store_types.length == 2
  raise 'We should have Registry as supported type' unless store_types.find { |store_type| store_type['label'] == 'registry' }
  raise 'We should have OS Image as supported type' unless store_types.find { |store_type| store_type['label'] == 'os_image' }

  stores = $api_test.image.store.list_image_stores
  log "Image Stores: #{stores}"
  registry = stores.find { |store| store['storetype'] == 'registry' }
  raise "Label #{registry['label']} is different than 'galaxy-registry'" unless registry['label'] == 'galaxy-registry'
  raise "URI #{registry['uri']} is different than '#{$no_auth_registry}'" unless registry['uri'] == $no_auth_registry.to_s
end

When(/^I set and get details of image store via API$/) do
  # test setDetails call
  # delete if test fail in the middle. delete image doesn't raise an error if image doesn't exists
  $api_test.image.store.create('Norimberga', 'https://github.com/uyuni-project/uyuni', 'registry')
  details_store = {}
  details_store['uri'] = 'Germania'
  details_store['username'] = ''
  details_store['password'] = ''
  $api_test.image.store.set_details('Norimberga', details_store)
  # test getDetails call
  details = $api_test.image.store.get_details('Norimberga')
  raise "uri should be Germania but is #{details['uri']}" unless details['uri'] == 'Germania'
  raise "username should be empty but is #{details['username']}" unless details['username'] == ''
  $api_test.image.store.delete('Norimberga')
end

# profiles tests
When(/^I create and delete profiles via API$/) do
  $api_test.image.profile.create('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  $api_test.image.profile.delete('fakeone')
  $api_test.image.profile.create('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '1-SUSE-KEY-x86_64')
  $api_test.image.profile.delete('fakeone')
end

When(/^I create and delete profile custom values via API$/) do
  $api_test.image.profile.create('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
  $api_test.system.custominfo.create_key('arancio', 'test containers')
  values = {}
  values['arancio'] = 'arancia API tests'
  $api_test.image.profile.set_custom_values('fakeone', values)
  pro_det = $api_test.image.profile.get_custom_values('fakeone')
  raise "setting custom profile value failed: #{pro_det['arancio']} != 'arancia API tests'" unless pro_det['arancio'] == 'arancia API tests'
  pro_type = $api_test.image.profile.list_image_profile_types
  raise "Number of image profile types is #{pro_type.length}" unless pro_type.length == 2
  raise "type #{pro_type[0]} is not dockerfile" unless pro_type[0] == 'dockerfile'
  raise "type #{pro_type[1]} is not kiwi" unless pro_type[1] == 'kiwi'
  key = ['arancio']
  $api_test.image.profile.delete_custom_values('fakeone', key)
end

When(/^I list image profiles via API$/) do
  ima_profiles = $api_test.image.profile.list_image_profiles
  log $ima_profiles
  imagelabel = ima_profiles.select { |image| image['label'] = 'fakeone' }
  raise "label of container should be fakeone! #{imagelabel[0]['label']} != 'fakeone'" unless imagelabel[0]['label'] == 'fakeone'
end

When(/^I set and get profile details via API$/) do
  details = {}
  details['storeLabel'] = 'galaxy-registry'
  details['path'] = 'TestForFun'
  details['activationKey'] = ''
  $api_test.image.profile.set_details('fakeone', details)
  cont_detail = $api_test.image.profile.get_details('fakeone')
  raise "label test fail! #{cont_detail['label']} != 'fakeone'" unless cont_detail['label'] == 'fakeone'
  raise "imagetype test fail! #{cont_detail['imageType']} != 'dockerfile'" unless cont_detail['imageType'] == 'dockerfile'
  $api_test.image.profile.delete('fakeone')
end
