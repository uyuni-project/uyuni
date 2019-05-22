<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>
        <rhn:require acl="user_role(satellite_admin)"/>
        <rhn:toolbar base="h1" icon="header-list" helpUrl="/docs/reference/admin/show-tomcat-logs.html">
          Tomcat
        </rhn:toolbar>
        <form action="/rhn/admin/Catalina.do">
            <rhn:csrf />
            <div class="panel panel-default">
                <div class="panel-heading">
                    <c:out value="${logfile_path}"/>
                </div>
                <div class="panel-body">
                    <textarea readonly rows="24" class="form-control">${contents}</textarea>
                </div>
            </div>
            <rhn:submitted/>
        </form>
    </body>
</html>
