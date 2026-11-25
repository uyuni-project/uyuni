# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT

# "access" namespace: custom roles (access groups) of role-based access control.
#
# Calls here authenticate as Credentials.for(host), i.e. the last identity logged into the web UI,
# not as admin like the user.* calls. Run them right after logging in as admin.
class NamespaceAccess
  # Initializes a new instance of the NamespaceAccess class.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  # Lists the roles of the organization of the logged-in user, predefined and custom.
  #
  # @return [Array<Hash>] The roles, each with a 'label' and a 'description'.
  def list_roles
    @test.call('access.listRoles', sessionKey: @test.token)
  end

  # Deletes a custom role.
  #
  # @param label [String] The label of the role.
  # @return [Integer] 1 on success.
  def delete_role(label)
    @test.call('access.deleteRole', sessionKey: @test.token, label: label)
  end

  # Creates a custom role, optionally copying the permissions of existing roles.
  #
  # @param label [String] The label of the new role.
  # @param description [String] The description of the new role.
  # @param permissions_from [Array<String>] The labels of the roles to inherit permissions from.
  # @return [Hash] The new role.
  def create_role(label, description, permissions_from = [])
    @test.call('access.createRole', sessionKey: @test.token, label: label, description: description, permissionsFrom: permissions_from)
  end

  # Lists all the namespaces known to the server.
  #
  # @return [Array<Hash>] The namespaces, each with a 'namespace', an 'access_mode' and a 'description'.
  def list_namespaces
    @test.call('access.listNamespaces', sessionKey: @test.token)
  end

  # Lists the permissions granted by a role.
  #
  # @param label [String] The label of the role.
  # @return [Array<Hash>] The namespaces granted, each with a 'namespace' and an 'access_mode'.
  def list_permissions(label)
    @test.call('access.listPermissions', sessionKey: @test.token, label: label)
  end

  # Grants access to namespaces for a role.
  #
  # @param label [String] The label of the role.
  # @param namespaces [Array<String>] The namespaces to grant.
  # @param modes [Array<String>] The access modes: 'R' (view) and/or 'W' (modify).
  # @return [Integer] 1 on success.
  def grant_access(label, namespaces, modes)
    @test.call('access.grantAccess', sessionKey: @test.token, label: label, namespaces: namespaces, modes: modes)
  end

  # Revokes access to namespaces from a role.
  #
  # @param label [String] The label of the role.
  # @param namespaces [Array<String>] The namespaces to revoke.
  # @param modes [Array<String>] The access modes: 'R' (view) and/or 'W' (modify).
  # @return [Integer] 1 on success.
  def revoke_access(label, namespaces, modes)
    @test.call('access.revokeAccess', sessionKey: @test.token, label: label, namespaces: namespaces, modes: modes)
  end

  # Tells whether a list of permissions contains a namespace with an access mode.
  # The XML-RPC API returns the access mode as a struct ({ 'value' => 'R' }) and the HTTP API as a plain
  # string ('R'), 'R' being View and 'W' Modify.
  #
  # @param permissions [Array<Hash>] The permissions, as returned by list_namespaces or list_permissions.
  # @param namespace [String] The namespace to look for.
  # @param mode [String] The access mode: 'R' or 'W'.
  # @return [Boolean] Whether the namespace is granted with that mode.
  def permission_listed?(permissions, namespace, mode)
    permissions.any? do |perm|
      access_mode = perm['access_mode']
      access_mode = access_mode['value'] if access_mode.is_a?(Hash)
      perm['namespace'] == namespace && access_mode == mode
    end
  end

  # Tells whether a role exists in the organization of the logged-in user.
  #
  # @param label [String] The label of the role.
  # @return [Boolean] Whether the role is listed.
  def role_present?(label)
    list_roles.any? { |role| role['label'] == label }
  end

  # Deletes a custom role when it exists, so that a test starts from a clean state.
  #
  # @param label [String] The label of the role.
  def remove_role_if_present(label)
    return unless role_present?(label)

    raise ScriptError, "The access group #{label} could not be deleted" unless delete_role(label) == 1
  end
end
