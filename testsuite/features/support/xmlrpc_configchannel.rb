require_relative 'xmlrpctest'

# configchannel class
class XMLRPCConfigChannelTest < XMLRPCBaseTest
  def channel_exists(channel)
    @connection.call('configchannel.channel_exists', @sid, channel)
  end

  def list_files(channel)
    @connection.call('configchannel.list_files', @sid, channel)
  end

  def list_subscribed_systems(channel)
    @connection.call('configchannel.list_subscribed_systems', @sid, channel)
  end

  def create_or_update_path(channel, file, contents)
    @connection.call('configchannel.create_or_update_path', @sid, channel, file, false,
                     {
                       "contents" => contents,
                       "owner" => "root",
                       "group" => "root",
                       "permissions" => "644"
                     }
                    )
  end

  def deploy_all_systems(channel)
    @connection.call('configchannel.deploy_all_systems', @sid, channel)
  end
end
