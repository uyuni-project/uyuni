<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>

<h2>
    <bean:message key="ssm.package.header"/>
</h2>

<div class="page-summary">
    <p><bean:message key="ssm.package.summary"/></p>
</div>

<ul>
    <rhn:require acl="all_systems_in_set_have_feature(ftr_package_updates)">
        <li><a href="PackageUpgrade.do"><bean:message key="ssm.package.upgrade"/></a></li>
    </rhn:require>
    <rhn:require acl="all_systems_in_set_have_feature(ftr_package_refresh)">
        <li><a href="PackageInstall.do"><bean:message key="ssm.package.install"/></a></li>
    </rhn:require>
    <rhn:require acl="all_systems_in_set_have_feature(ftr_package_remove)">
        <li><a href="PackageRemove.do"><bean:message key="ssm.package.remove"/></a></li>
    </rhn:require>
    <rhn:require acl="all_systems_in_set_have_feature(ftr_package_verify)">
        <li><a href="PackageVerify.do"><bean:message key="ssm.package.verify"/></a></li>
    </rhn:require>
</ul>

</body>
</html>
