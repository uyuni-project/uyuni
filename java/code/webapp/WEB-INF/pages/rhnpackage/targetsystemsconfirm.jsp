<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html:html >
<body>
<%@ include file="/WEB-INF/pages/common/fragments/package/package_header.jspf" %>

<h2>
<bean:message key="targetsystemsconfirm.jsp.title"/>
</h2>

<div>

<rl:listset name="systemSet" legend="system">
<rhn:csrf />
<rhn:submitted />
    <div class="spacewalk-section-toolbar">
        <div class="action-button-wrapper">
            <input type="submit" class="btn btn-success" name="dispatch" value='<bean:message key="confirm.jsp.confirm"/>'/>
        </div>
    </div>

    <jsp:include page="/WEB-INF/pages/common/fragments/schedule-options.jspf"/>

        <rl:list
                dataset="pageList"
                name="systemList"
                emptykey="nosystems.message"
                alphabarcolumn="name"
                filter="com.redhat.rhn.frontend.taglibs.list.filters.SystemOverviewFilter"
                >
        <rl:decorator name="PageSizeDecorator"/>

                <!-- Name Column -->
                <rl:column sortable="false"
                           bound="false"
                           headerkey="systemlist.jsp.system"
                           sortattr="name"
                           defaultsort="asc"
                           styleclass="${namestyle}">
                        <%@ include file="/WEB-INF/pages/common/fragments/systems/system_list_fragment.jspf" %>
                </rl:column>
         </rl:list>
</rl:listset>

</div>

</body>
</html:html>
