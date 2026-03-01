<%--
    Document   : reboot-systems
    Created on : Jul 16, 2013, 2:35:54 PM
    Author     : Bo Maryniuk <bo@suse.de>
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <body>
        <%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
        <h2><bean:message key="ssm.misc.reboot.header" /></h2>
        <p><bean:message key="ssm.misc.reboot.summary" /></p>

        <rl:listset name="systemsListSet" legend="system">
            <div class="spacewalk-section-toolbar">
                <div class="action-button-wrapper">
                    <html:submit styleClass="btn btn-default" property="dispatch">
                        <bean:message key="ssm.misc.reboot.operationname" />
                    </html:submit>
                </div>
            </div>
            <c:set var="noCsv" value="1" />
            <c:set var="noAddToSsm" value="1" />
            <%@ include file="/WEB-INF/pages/common/fragments/systems/system_listdisplay.jspf" %>
        </rl:listset>
    </body>
</html>
