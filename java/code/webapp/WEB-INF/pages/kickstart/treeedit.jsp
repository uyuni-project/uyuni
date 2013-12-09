<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >
    <head>
        <meta http-equiv="Pragma" content="no-cache" />
        <script language="javascript" src="/javascript/refresh.js"></script>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="fa-rocket"
                     deletionUrl="/rhn/kickstart/TreeDelete.do?kstid=${kstree.id}"
                     deletionType="deleteTree"
                     imgAlt="kickstarts.alt.img">
            <bean:message key="treeedit.jsp.toolbar"/>
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/kickstart_tree_details.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <h2><bean:message key="treeedit.jsp.header2"/></h2>
        <bean:message key="treecreate.jsp.header1"/>
        <html:form method="post" action="/kickstart/TreeEdit.do" styleClass="form-horizontal">
            <rhn:csrf />
            <%@ include file="tree-form.jspf" %>
            <c:if test="${requestScope.hidesubmit != 'true'}">
                <div class="col-md-offset-3 col-md-6">
                    <html:submit styleClass="btn btn-success">
                        <bean:message key="edittree.jsp.submit"/>
                    </html:submit>
                </div>
            </c:if>
    <html:hidden property="submitted" value="true"/>
    <html:hidden property="kstid" value="${kstid}"/>
    </html:form>
</body>
</html:html>

