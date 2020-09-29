## manage orgs
my_org:
  uyuni.org_present:
    - name: my_org
    - org_admin_user: my_org_user
    - org_admin_password: my_org_user
    - first_name: first_name
    - last_name: last_name__
    - email: my_org_user@org.com
    - admin_user: admin
    - admin_password: admin

org_trust_present:
  uyuni.org_trust:
    - org_name: SUSE
    - trusts:
      - my_org

# manager system groups
system_group_httpd:
  uyuni.group_present:
    - name: httpd_servers
    - description: httpd_servers
    - target: "*httpd*"
    - org_admin_user: my_org_user
    - org_admin_password: my_org_user

#manager users
user_1:
  uyuni.user_present:
    - name: user1
    - password: user1
    - email: user1@teest.como
    - first_name: first
    - last_name: last
    - org_admin_user: my_org_user
    - org_admin_password: my_org_user
    - roles: ["system_group_admin", "channel_admin"]
    - system_groups:
      - httpd_servers

user_1_channels:
  ## remane it to user_channels (without _present)
  uyuni.user_channels:
    - name: user1
    - password: user1
    - org_admin_user: my_org_user
    - org_admin_password: my_org_user
    - manageable_channels:
      - my_local_channel
    - subscribable_channels:
      - new_local

define_custom_activation_key:
    uyuni.activation_key_present:
        - name: my-suse
        - description: "My Activation Key created via Salt"
        - org_admin_user: my_org_user
        - org_admin_password: my_org_user
        - base_channel: sle-product-sles15-sp2-pool-x86_64
        - child_channels:
            - sle-module-server-applications15-sp2-pool-x86_64
            - sle-module-server-applications15-sp2-updates-x86_64
        - configuration_channels:
            - firewall
        - packages:
            - name: emacs
              arch: x86_64
        - server_groups:
            - httpd_servers
        - usage_limit: 10
        - system_types:
            - virtualization_host
        - configure_after_registration: true
