<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>

    <h2>
        <rhn:icon type="header-errata" title="errata.common.errataAlt"/>
        <bean:message key="header.jsp.errata.add"/>
    </h2>

    <dl class="dl-horizontal">
        <dt>
            <a href="/rhn/channels/manage/errata/AddRedHatErrata.do?cid=${cid}">
                <bean:message key="channel.manage.errata.addredhaterrata"/>
            </a>
        </dt>
        <dd>
            <bean:message key="channel.manage.errata.addredhaterratamsg"/>
        </dd>

        <dt>
            <a href="/rhn/channels/manage/errata/AddCustomErrata.do?cid=${cid}">
                <bean:message key="channel.manage.errata.addcustomerrata"/>
            </a>
        </dt>
        <dd>
            <bean:message key="channel.manage.errata.addcustomerratamsg"/>
        </dd>

        <dt>
            <a href="/rhn/errata/manage/Create.do">
                <bean:message key="channel.manage.errata.createerrata"/>
            </a>
        </dt>
        <dd>
            <bean:message key="channel.manage.errata.createerratamsg"/>
        </dd>
    </dl>

</body>
</html>
