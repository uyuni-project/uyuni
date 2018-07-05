<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<body>

<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>

<h2><bean:message key="ssm.overview.header"/></h2>
<div class="page-summary">
    <ul><bean:message key="ssm.overview.summary"/></ul>
    <ul><bean:message key="ssm.overview.summary2"/></ul>
</div>


<div class="panel panel-default">
    <ul class="list-group">
        <li class="list-group-item">
            <div class="row">
                <div class="col-sm-2">
                    <rhn:icon type="header-system" title="ssm.overview.systems" />
                    <bean:message key="ssm.overview.systems"/>
                </div>
                <div class="col-sm-10">
                    <bean:message key="ssm.overview.systems.list"/>
                </div>
            </div>
        </li>
        <rhn:require acl="all_systems_in_set_have_feature(ftr_errata_updates)">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                        <rhn:icon type="header-errata" title="ssm.overview.errata" />
                        <bean:message key="ssm.overview.errata"/>
                    </div>
                    <div class="col-sm-10">
                        <bean:message key="ssm.overview.errata.schedule"/>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="all_systems_in_set_have_feature(ftr_package_updates)">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                      <rhn:icon type="header-package" title="ssm.overview.packages" />
                        <bean:message key="ssm.overview.packages"/>
                    </div>
                    <div class="col-sm-10">
                        <bean:message key="ssm.overview.packages.upgrade"/>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_package_refresh)">
                            / <bean:message key="ssm.overview.packages.install"/>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_package_remove)">
                            / <bean:message key="ssm.overview.packages.remove"/>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_package_verify)">
                            / <bean:message key="ssm.overview.packages.verify"/>
                        </rhn:require>
                        <bean:message key="ssm.overview.packages"/>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="any_system_with_salt_entitlement()">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                        <rhn:icon type="system-state" title="ssm.overview.states" />
                        <bean:message key="ssm.overview.states"/>
                    </div>
                    <div class="col-sm-10">
                        <bean:message key="ssm.overview.states.apply"/>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="user_role(org_admin); all_systems_in_set_have_feature(ftr_system_grouping)">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                        <rhn:icon type="header-system-groups" title="ssm.overview.groups" />
                        <bean:message key="ssm.overview.groups"/>
                    </div>
                    <div class="col-sm-10">
                        <bean:message key="ssm.overview.groups.create"/>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="all_systems_in_set_have_feature(ftr_channel_membership)">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                      <rhn:icon type="header-channel" title="ssm.overview.channels" />
                        <bean:message key="ssm.overview.channels"/>
                    </div>
                    <div class="col-sm-10">
                        <ul class="list-unstyled">
                            <li><bean:message key="ssm.overview.channels.memberships"/></li>
                            <rhn:require acl="user_role(config_admin);
                                    all_systems_in_set_have_feature(ftr_channel_config_subscription)">
                                <li><bean:message key="ssm.overview.channels.subscriptions"/></li>
                            </rhn:require>
                            <rhn:require acl="user_role(config_admin);
                                    all_systems_in_set_have_feature(ftr_channel_deploy_diff)">
                                <li><bean:message key="ssm.overview.channels.deploy"/></li>
                            </rhn:require>
                        </ul>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="all_systems_in_set_have_feature(ftr_kickstart)">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                      <rhn:icon type="header-kickstart" title="ssm.overview.provisioning" />
                        <bean:message key="ssm.overview.provisioning"/>
                    </div>
                    <div class="col-sm-10">
                        <ul class="list-unstyled">
                            <rhn:require acl="all_systems_in_set_have_feature(ftr_kickstart)">
                                <li><bean:message key="ssm.overview.provisioning.kickstart"/></li>
                            </rhn:require>
                            <rhn:require acl="all_systems_in_set_have_feature(ftr_tag_system)">
                                <li><bean:message key="ssm.overview.provisioning.rollback"/></li>
                            </rhn:require>
                            <rhn:require acl="all_systems_in_set_have_feature(ftr_power_management)">
                                <li><bean:message key="ssm.overview.provisioning.powermanagement.configure"/></li>
                                <li><bean:message key="ssm.overview.provisioning.powermanagement.operations"/></li>
                            </rhn:require>
                        </ul>
                    </div>
                </div>
            </li>
        </rhn:require>
        <rhn:require acl="not all_systems_in_set_have_feature(ftr_kickstart); any_system_with_salt_entitlement()">
            <li class="list-group-item">
                <div class="row">
                    <div class="col-sm-2">
                      <rhn:icon type="header-kickstart" title="ssm.overview.provisioning" />
                        <bean:message key="ssm.overview.provisioning"/>
                    </div>
                    <div class="col-sm-10">
                        <ul class="list-unstyled">
                            <rhn:require acl="all_systems_in_set_have_feature(ftr_power_management)">
                                <li><bean:message key="ssm.overview.provisioning.powermanagement.configure"/></li>
                                <li><bean:message key="ssm.overview.provisioning.powermanagement.operations"/></li>
                            </rhn:require>
                        </ul>
                    </div>
                </div>
            </li>
        </rhn:require>
        <li class="list-group-item">
            <div class="row">
                <div class="col-sm-2">
                  <rhn:icon type="header-event-history" title="ssm.overview.misc" />
                    <bean:message key="ssm.overview.misc"/>
                </div>
                <div class="col-sm-10">
                    <ul class="list-unstyled">
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_system_preferences)">
                            <li><bean:message key="ssm.overview.misc.systempreferences"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_hardware_refresh)">
                            <li><bean:message key="ssm.overview.misc.hardwareprofiles"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_package_refresh)">
                            <li><bean:message key="ssm.overview.misc.packageprofiles"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_remote_command)">
                            <li><bean:message key="ssm.overview.provisioning.remotecommands"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_system_custom_values)">
                            <li><bean:message key="ssm.overview.misc.customvalues"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_system_lock)">
                            <li><bean:message key="ssm.overview.misc.lock"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_system_audit)">
                            <li><bean:message key="ssm.overview.misc.scap"/></li>
                        </rhn:require>
                        <rhn:require acl="all_systems_in_set_have_feature(ftr_reboot)">
                            <li><bean:message key="ssm.overview.misc.reboot"/></li>
                        </rhn:require>
                        <li><bean:message key="ssm.overview.misc.migrate"/></li>
                        <li><bean:message key="ssm.overview.misc.delete"/></li>
                    </ul>
                </div>
            </div>
        </li>
    </ul>
</div>

</body>
</html>
