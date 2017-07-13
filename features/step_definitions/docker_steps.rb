# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

require "xmlrpc/client"
require 'time'
require 'date'
require 'securerandom'
require 'timeout'

# this module test image_profile
module ImageProfile
  def create_delete
    cont_op = XMLRPCImageTest.new(ENV['TESTHOST'])
    cont_op.login('admin', 'admin')
    # create delete profile test
    cont_op.createProfile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
    cont_op.deleteProfile('fakeone')
    cont_op.createProfile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '1-DOCKER-TEST')
    cont_op.deleteProfile('fakeone')
  end

  def custom_values
    cont_op = XMLRPCImageTest.new(ENV['TESTHOST'])
    cont_op.login('admin', 'admin')
    cont_op.createProfile('fakeone', 'dockerfile', 'galaxy-registry', 'BiggerPathBiggerTest', '')
    cont_op.createCustomKey('arancio', 'test containers')
    values = {}
    values['arancio'] = 'arancia xmlrpc tests'
    cont_op.setProfileCustomValues('fakeone', values)
    pro_det = cont_op.getProfileCustomValues('fakeone')
    puts pro_det
    assert_equal(pro_det['arancio'], 'arancia xmlrpc tests', 'setting custom profile value failed')
    pro_type = cont_op.listImageProfileTypes
    assert_equal(pro_type.length, 1, 'support at moment only one type of Profile!')
    assert_equal(pro_type[0], 'dockerfile', 'type is not dockerfile?')
    key = ['arancio']
    cont_op.deleteProfileCustomValues('fakeone', key)
  end

  def image_profiles_xmlrpc
    cont_op = XMLRPCImageTest.new(ENV['TESTHOST'])
    cont_op.login('admin', 'admin')
    # create delete tests
    create_delete
    # set get delete Custom Values
    custom_values
    puts cont_op.listImageProfiles
    # test listImageProfiles method
    ima_profiles = cont_op.listImageProfiles
    imagelabel = ima_profiles.select { |image| image['label'] = 'fakeone' }
    assert_equal(imagelabel[0]['label'], 'fakeone', "label of container should be fakeone!")
    # test set value and get value call
    details = {}
    details['storeLabel'] = 'galaxy-registry'
    details['path'] = 'TestForFun'
    details['activationKey'] = ''
    cont_op.setProfileDetails('fakeone', details)
    cont_detail = cont_op.getDetails('fakeone')
    assert_equal(cont_detail['label'], 'fakeone', 'label test fail!')
    assert_equal(cont_detail['imageType'], 'dockerfile', 'imagetype test fail!')
    cont_op.deleteProfile('fakeone')
  end

  # FIXME: implement random cration of profiles
  def create_random_profile(num)
    puts num
  end
end

World(ImageProfile)

# container_operations
cont_op = XMLRPCImageTest.new(ENV['TESTHOST'])
# retrieve minion id, needed for scheduleImageBuild call
def retrieve_minion_id
  sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.listSystems
  refute_nil(systems)
  minion_id = systems
              .select { |s| s['name'] == $minion_fullhostname }
              .map { |s| s['id'] }.first
  refute_nil(minion_id, "Minion #{$minion_fullhostname} is not yet registered?")
  minion_id
end

And(/^I select sle-minion hostname in Build Host$/) do
  select($minion_fullhostname, :from => 'buildHostId')
end

And(/^I navigate to images webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/images")
end

And(/^I navigate to images build webpage$/) do
  visit("https://#{$server_fullhostname}/rhn/manager/cm/build")
end

And(/^I verify that all "([^"]*)" container images were built correctly in the gui$/) do |count|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      raise "error detected while building images" if has_xpath?("//*[contains(@title, 'Failed')]")
      break if has_xpath?("//*[contains(@title, 'Built')]", :count => count)
      sleep 5
      step %(I navigate to images webpage)
    end
  rescue Timeout::Error
    raise "at least one image was not built correctly"
  end
end

And(/^I schedule the build of image "([^"]*)" via xmlrpc-call$/) do |image|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = ''
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.scheduleImageBuild(image, version_build, build_hostid, date_build)
end

And(/^I schedule the build of image "([^"]*)" with version "([^"]*)" via xmlrpc-call$/) do |image, version|
  cont_op.login('admin', 'admin')
  # empty by default
  version_build = version
  build_hostid = retrieve_minion_id
  now = DateTime.now
  date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  cont_op.scheduleImageBuild(image, version_build, build_hostid, date_build)
end

And(/^I delete the image "([^"]*)" with version "([^"]*)" via xmlrpc-call$/) do |image_name_todel, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.listImages
  refute_nil(images_list, "ERROR: no images at all were retrieved.")
  images_list.each do |element|
    if element['name'] == image_name_todel.strip && element['version'] == version.strip
      $image_id = element['id']
    end
  end
  cont_op.deleteImage($image_id)
end

And(/^The image "([^"]*)" with version "([^"]*)" doesn't exist via xmlrpc-call$/) do |image_non_exist, version|
  cont_op.login('admin', 'admin')
  images_list = cont_op.listImages
  images_list.each do |element|
    raise "#{image_non_exist} should not exist anymore" if element['name'] == image_non_exist && element['version'] == version.strip
  end
end

# images stores tests
And(/^I run image.store tests via xmlrpc$/) do
  cont_op.login('admin', 'admin')
  # Test create and delete calls
  # create and delete a store, even with invalid uri.
  cont_op.createStore('fake_store', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  cont_op.deleteStore('fake_store')
  # test list images and list image types call
  store_typ = cont_op.listImageStoreTypes
  assert_equal(store_typ.length, 1, 'we have only type support for Registry! New method added?! please update the tests')
  assert_equal(store_typ[0]['label'], 'registry', 'imagestore label type should be registry!')
  registry_list = cont_op.listImageStores
  # print just for debug
  puts registry_list
  assert_equal(registry_list[0]['label'], 'galaxy-registry', 'label is galaxy!')
  assert_equal(registry_list[0]['uri'], 'registry.mgr.suse.de', 'uri should be registry.mgr.suse.de')
  # test setDetails call
  # delete if test fail in the middle. delete image doesn't raise an error if image doesn't exists
  cont_op.createStore('Norimberga', 'https://github.com/SUSE/spacewalk-testsuite-base', 'registry')
  details_store = {}
  details_store['uri'] = 'Germania'
  details_store['username'] = ''
  details_store['password'] = ''
  cont_op.setDetails('Norimberga', details_store)
  # test getDetails call
  details = cont_op.getDetailsStore('Norimberga')
  assert_equal(details['uri'], 'Germania', 'uri should be Germania')
  assert_equal(details['username'], '', 'username should be empty')
  cont_op.deleteStore('Norimberga')
end

And(/^I create "([^"]*)" random image stores$/) do |count|
  cont_op.login('admin', 'admin')
  $labels = []
  count.to_i.times do
    label = SecureRandom.urlsafe_base64(10)
    $labels.push(label)
    uri = SecureRandom.urlsafe_base64(13)
    cont_op.createStore(label, uri, 'registry')
  end
end

And(/^I delete the random image stores$/) do
  cont_op.login('admin', 'admin')
  $labels.each do |label|
    cont_op.deleteStore(label)
  end
end

# Profiles tests using module
And(/^I run image.profiles tests via xmlrpc$/, :image_profiles_xmlrpc)

Then(/I create "([^"]*)" random "([^"]*)" containers$/) do |count, image_input|
  cont_op.login('admin', 'admin')
  image = image_input
  build_hostid = retrieve_minion_id
  count.to_i.times do
    version_build = SecureRandom.urlsafe_base64(10)
    now = DateTime.now
    date_build = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
    cont_op.scheduleImageBuild(image, version_build, build_hostid, date_build)
  end
end

And(/^I check that sles-minion exists otherwise bootstrap it$/) do
  ck_minion =  "salt #{$minion_fullhostname} test.ping"
  _out, code = $server.run(ck_minion, false)
  if code.nonzero?
    # bootstrap minion
    steps %(
      Given I am authorized
      When I follow "Salt"
      Then I should see a "Bootstrapping" text
      And I follow "Bootstrapping"
      Then I should see a "Bootstrap Minions" text
      And  I enter the hostname of "sle-minion" as hostname
      And I enter "22" as "port"
      And I enter "root" as "user"
      And I enter "linux" as "password"
      And I click on "Bootstrap"
      And I wait for "150" seconds
      Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
    )
  end
end
