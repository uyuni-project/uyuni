<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >

<body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
    <br />

    <div class="toolbar-h2">
        <div class="toolbar">
            <span class="toolbar">
                <a href="/rhn/systems/details/SoftwareCrashDelete.do?crid=${crid}&sid=${sid}">
                    <rhn:icon type="item-del" title="toolbar.delete.crash" />
                    <bean:message key="toolbar.delete.crash"/>
                </a>
            </span>
        </div>
        <rhn:icon type="header-crash" title="info.alt.img" />
        ${fn:escapeXml(crash.crash)}
    </div>

    <br />
    <br />
    <%@ include file="/WEB-INF/pages/common/fragments/systems/crash-header.jspf" %>
    <div class="page-summary">
      <p><bean:message key="details.crashnotes.delete.confirm"/></p>
    </div>
    <html:form method="post" action="/systems/details/DeleteCrashNote.do">
        <rhn:csrf />
        <html:hidden property="submitted" value="true"/>
        <html:hidden property="sid" value="${sid}"/>
        <html:hidden property="crid" value="${crid}"/>
        <html:hidden property="cnid" value="${cnid}"/>
        <table class="details">
            <tr>
                <th><bean:message key="sdc.details.notes.subject"/></th>
                <td>${subject}</td>
            </tr>
            <tr>
                <th><bean:message key="sdc.details.notes.details"/></th>
                <td>${note}</td>
            </tr>
        </table>

        <hr/>
        <div class="text-right">
            <html:submit styleClass="btn btn-default">
                <bean:message key="sdc.details.notes.delete"/>
            </html:submit>
        </div>
    </html:form>
</body>
</html:html>
