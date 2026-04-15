<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html:html >
  <body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

    <rhn:toolbar base="h2" icon="header-note"
           creationUrl="/rhn/systems/details/EditNote.do?sid=${system.id}"
           creationType="note">
      <bean:message key="sdc.details.notes.header"/>
    </rhn:toolbar>

    <p><bean:message key="sdc.details.notes.message"/></p>

    <rl:listset name="groupSet">
        <rhn:csrf />
        <rhn:submitted />

        <rl:list dataset="pageList"
                width="100%"
                name="notesList"
                emptykey="sdc.details.notes.nonotes">

            <rl:column headerkey="sdc.details.notes.subject"
                sortable="true"
                width="35%"
                sortattr="subject">
                <a href="/rhn/systems/details/EditNote.do?sid=${system.id}&nid=${current.id}">
                    <c:out value="${current.subject}" escapeXml="true" />
                </a>
            </rl:column>

            <rl:column headerkey="sdc.details.notes.details" width="50%">
              <pre><c:out value="${current.note}" escapeXml="true" /></pre>
            </rl:column>

            <rl:column headerkey="sdc.details.notes.updated"
                sortable="true"
                sortattr="modified">
              <rhn:formatDate humanStyle="calendar" value="${current.modified}" type="both" dateStyle="short" timeStyle="long"/>
            </rl:column>

        </rl:list>
    </rl:listset>

  </body>
</html:html>
