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