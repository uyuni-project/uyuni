# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "configchannel" namespace
class NamespaceConfigchannel
  def initialize(api_test)
    @test = api_test
  end

  def channel_exists(channel)
    @test.call('configchannel.channelExists', sessionKey: @test.token, label: channel)
  end

  def list_files(channel)
    @test.call('configchannel.listFiles', sessionKey: @test.token, label: channel)
  end

  def list_subscribed_systems(channel)
    @test.call('configchannel.listSubscribedSystems', sessionKey: @test.token, label: channel)
  end

  def get_file_revision(channel, file_path, revision)
    @test.call('configchannel.getFileRevision', sessionKey: @test.token, label: channel, filePath: file_path, revision: revision)
  end

  def create(label, name, description, type)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type)
  end

  def create_with_pathinfo(label, name, description, type, info)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type, pathInfo: info)
  end

  def create_or_update_path(channel, file, contents)
    @test.call('configchannel.createOrUpdatePath',
               sessionKey: @test.token,
               label: channel,
               path: file,
               isDir: false,
               pathInfo: { contents: contents,
                           owner: 'root',
                           group: 'root',
                           permissions: '644' }
              )
  end

  def deploy_all_systems(channel)
    @test.call('configchannel.deployAllSystems', sessionKey: @test.token, label: channel)
  end

  def delete_channels(channels)
    @test.call('configchannel.deleteChannels', sessionKey: @test.token, labels: channels)
  end
end
