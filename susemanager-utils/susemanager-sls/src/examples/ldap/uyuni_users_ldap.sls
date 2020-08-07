
## Create organizations based on static pillar data
{% set org_auth = {} %}

{% for org in pillar.get('uyuni', {}).get('orgs', []) %}
{{org['org_id']}}:
  uyuni.org_present:
    - name: {{org['org_id']}}
    - org_admin_user: {{org['org_admin_user']}}
    - org_admin_password: {{org['org_admin_password']}}
    - first_name: {{org['first_name']}}
    - last_name: {{org['last_name']}}
    - email: {{org['email']}}
{% set _ = org_auth.update({org.org_id: {'org_admin_user': org.org_admin_user,  'org_admin_password': org.org_admin_password }}) %}
{% endfor %}

## load available roles to local map variable
## those where extracted form ldap to pillar
{% set roles_map = {} %}
{% for role in pillar.get('ldap-roles', []) %}
{% set _ = roles_map.update({role.dn: role.cn}) %}
{% endfor %}

{% for user in pillar.get('ldap-users', []) %}

  {% set admin_user = None %}
  {% set admin_password = None %}
  {% if org_auth[user['ou']] %}
    {% set admin_user = org_auth[user['ou']].org_admin_user %}
    {% set admin_password = org_auth[user['ou']].org_admin_password %}
  {% endif %}

{{user['uid']}}:
  uyuni.user_present:
    - name: {{user['uid']}}
    - password: 'dummy_local_pass'
    - email: {{user['mail']}}
    - first_name: {{user['givenName']}}
    - last_name: {{user['sn']}}
    - use_pam_auth: true
    - org_admin_user: {{admin_user}}
    - org_admin_password: {{admin_password}}
    {% if user['memberOf'] %}
    - roles:
      {% for user_role in user['memberOf'] %}
      - {{ roles_map[user_role] }}
      {% endfor %}
    {% endif %}

{% endfor %}
