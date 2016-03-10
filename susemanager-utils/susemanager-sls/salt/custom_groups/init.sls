#!py
def run():
    return {'include':
            ['custom.group_{0}'.format(gid) for gid in __pillar__.get('group_id', [])
             if __salt__['file.file_exists']('/srv/susemanager/salt/custom/group_{0}.sls'.format(gid))]
            }


