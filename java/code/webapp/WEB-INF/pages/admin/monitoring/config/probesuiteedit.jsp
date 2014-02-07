<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>
    <head>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-system-groups"
                     helpUrl="/rhn/help/reference/en-US/s1-sm-monitor.jsp#s2-sm-monitor-psuites">
            <bean:message key="probesuiteedit.jsp.header1" arg0="${probeSuite.suiteName}" />
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/probesuite_detail_edit.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <h2><bean:message key="probesuiteedit.jsp.header"/></h2>
        <html:form action="/monitoring/config/ProbeSuiteEdit" method="POST" styleClass="form-horizontal">
            <rhn:csrf />
            <rhn:submitted />
            <%@ include file="suite-form.jspf" %>
            <div class="form-group">
                <div class="col-lg-offset-3 col-lg-6">
                <html:submit styleClass="btn btn-success">
                    <bean:message key="probesuiteedit.jsp.updatesuite"/>
                </html:submit>
                </div>
            </div>
        <html:hidden property="suite_id" value="${probeSuite.id}"/>
        <html:hidden property="submitted" value="true"/>
    </html:form>

</body>
</html>
