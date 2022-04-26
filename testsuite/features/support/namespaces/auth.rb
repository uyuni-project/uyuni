# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "auth" namespace
class NamespaceAuth
  def initialize(api_test)
    @test = api_test
  end

  def login(user, password)
    @test.token = @test.call('auth.login', login: user, password: password)
  end

  # log out from API
  def logout
    @test.call('auth.logout', sessionKey: @test.token)
  end
end
