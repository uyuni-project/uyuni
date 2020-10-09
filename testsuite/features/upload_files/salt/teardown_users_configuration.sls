my_org_remove:
  uyuni.org_absent:
    - name: my_org
    - admin_user: admin
    - admin_password: admin

my_org2_remove:
  uyuni.org_absent:
    - name: my_org2
    - admin_user: admin
    - admin_password: admin

minions_group_absent:
  uyuni.group_absent:
    - name: minions_group
    - org_admin_user: admin
    - org_admin_password: admin

user2_absent:
  uyuni.user_absent:
    - name: user2
    - org_admin_user: admin
    - org_admin_password: admin

delete_obsolete_activation_key:
    uyuni.activation_key_absent:
        - name: my-suse
        - org_admin_user: admin
        - org_admin_password: admin

