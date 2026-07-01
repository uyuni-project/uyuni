<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<rhn:require acl="authorized_for(admin.config)">
  <c:if test="${requestScope.systemdiskwarning}">
    <div class="alert alert-warning">
      <bean:message key="notification.diskcheck.details" arg0="system" arg1="warning" />
    </div>
  </c:if>
  <c:if test="${requestScope.dbdiskwarning}">
    <div class="alert alert-warning">
      <bean:message key="notification.diskcheck.details" arg0="database" arg1="warning" />
    </div>
  </c:if>
</rhn:require>
