<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<html:html>
    <body>
        <rhn:toolbar base="h1" icon="fa-info-circle">
            <bean:message key="iss.confirmslaveremove.jsp.toolbar" />
        </rhn:toolbar>
        <p><bean:message key="iss.confirmslaveremove.jsp.areyousure" /></p>
        <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/iss_config.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <c:set var="pageList" value="${requestScope.all}" />
        <h2><bean:message key="iss.confirmslaveremove.jsp.header2" /></h2>
        <rl:listset name="issSlaveListSet">
            <rhn:csrf />
            <rhn:submitted />
            <rl:list dataset="pageList" name="issSlaveList"
                     emptykey="iss.confirmslaveremove.jsp.nomasters">
                <rl:column sortable="true" headerkey="iss.slave.name" sortattr="slave">
                    <html:link href="/rhn/admin/iss/EditSlave.do?sid=${current.id}">
                        <c:out value="${current.slave}" />
                    </html:link>
                </rl:column>
                <rl:column bound="false" headerkey="iss.slave.isEnabled"
                           sortattr="enabled">
                    <c:if test="${current.enabled == 'Y'}">
                        <i class="fa fa-check text-success" title="<bean:message key='iss.slave.enabled'/>"></i>
                    </c:if>
                    <c:if test="${current.enabled != 'Y'}">
                        <i class="fa fa-circle-o" title="<bean:message key='iss.slave.disabled'/>"></i>
                    </c:if>
                </rl:column>
                <rl:column bound="false" headerkey="iss.slave.toAll"
                           sortattr="allOrgs">
                    <c:if test="${current.allowAllOrgs == 'Y'}">
                        <i class="fa fa-check text-success" title="<bean:message key='iss.slave.all'/>"></i>
                    </c:if>
                    <c:if test="${current.allowAllOrgs != 'Y'}">
                        <i class="fa fa-circle-o" title="<bean:message key='iss.slave.notAll'/>"></i>
                    </c:if>
                </rl:column>
            </rl:list>
            <c:if test="${not empty requestScope.all}">
                <div class="text-right">
                    <rhn:submitted />
                    <input type="submit" name="dispatch" class="btn btn-success"
                           value='<bean:message key="iss.confirm.remove.slaves"/>' />
                </div>
            </c:if>
        </rl:listset>
    </body>
</html:html>
