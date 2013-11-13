<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>    
    <body>
        <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
        <h2>
            <img src="/img/rhn-icon-package_add.gif" alt="<bean:message key='installpkgs.jsp.installpackages'/>" />
            <bean:message key="pkg.lock.header" />
        </h2>
        <p><bean:message key="pkg.lock.summary" /></p>
        
        <rl:listset name="packageListSet">
            <rhn:csrf />
            <rl:list dataset="dataset"
                     width="100%"
                     name="packageList"
                     styleclass="list"
                     emptykey="packagelist.jsp.nopackages"
                     alphabarcolumn="nvre">
                <rl:decorator name="PageSizeDecorator"/>
                <rl:decorator name="ElaborationDecorator"/>
                <rl:decorator name="SelectableDecorator"/>
                <rl:selectablecolumn value="${current.selectionKey}"
                                     selected="${current.selected}"
                                     disabled="${not current.selectable}"/>
                
                <rl:column headerkey="packagelist.jsp.packagename"
                           bound="false"
                           sortattr="nvre"
                           sortable="true"
                           filterattr="nvre"
                           styleclass="">
                    <c:choose>
                        <c:when test="${not checkPackageId or not empty current.packageId}">
                            <a href="/rhn/software/packages/Details.do?sid=${param.sid}&amp;id_combo=${current.idCombo}0">${current.nvre}</a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${current.nvre}"/>
                        </c:otherwise>
                    </c:choose>
                </rl:column>
                <rl:column headerkey="packagelist.jsp.packagearch" bound="false" styleclass="thin-column last-column">
                    <c:choose>
                        <c:when test ="${not empty current.arch}">${current.arch}</c:when>
                        <c:otherwise><bean:message key="packagelist.jsp.notspecified"/></c:otherwise>
                    </c:choose>
                </rl:column>
            </rl:list>

            <div align="right">
                <rhn:submitted/>
                <input type="submit" name ="dispatch" value='<bean:message key="pkg.lock.requestlock"/>'/>
                <input type="submit" name ="dispatch" value='<bean:message key="pkg.lock.requestunlock"/>'/>
            </div>
        </rl:listset>
    </body>
</html>
