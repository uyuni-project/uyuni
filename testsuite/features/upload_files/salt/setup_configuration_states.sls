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

my_org2:
  uyuni.org_present:
    - name: my_org2
    - org_admin_user: my_org_user2
    - org_admin_password: my_org_user2
    - first_name: first_name
    - last_name: last_name_
    - email: my_org_user2@org.com
    - admin_user: admin
    - admin_password: admin

org_trust_present:
  uyuni.org_trust:
    - org_name: my_org
    - admin_user: admin
    - admin_password: admin
    - trusts:
      - my_org2


minions_group_present:
  uyuni.group_present:
    - name: minions_group
    - description: httpd_servers
    - expression: "*min*"
    - org_admin_user: admin
    - org_admin_password: admin

user_2:
  uyuni.user_present:
    - name: user2
    - password: user2
    - email: user1@teest.como
    - first_name: first
    - last_name: last
    - org_admin_user: admin
    - org_admin_password: admin
    - roles: ["activation_key_admin", "config_admin"]
    - system_groups:
      - minions_group

user_2_channels:
  uyuni.user_channels:
    - name: user2
    - password: user2
    - org_admin_user: admin
    - org_admin_password: admin
    - manageable_channels:
      - test-channel-x86_64
    - subscribable_channels:
      - test_base_channel


