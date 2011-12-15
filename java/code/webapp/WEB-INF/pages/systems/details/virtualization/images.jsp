<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>
    <body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
    <br/>

	<rhn:toolbar base="h1" img="/img/susestudio.png"></rhn:toolbar>

	<div class="page-summary">
      <p>Please choose one of the available SUSE Studio images below for deployment to this virtual host.</p>
	</div>

    <rl:listset name="groupSet">
        <rhn:csrf />
		<html:hidden property="sid" value="${param.sid}" />
        <rl:list dataset="pageList" 
                 emptykey="studio.images.list.noimages">
            <rl:radiocolumn value="${current.id}" styleclass="first-column"/>
            <rl:column headerkey="studio.images.list.name">
                ${current.name}
            </rl:column>
            <rl:column headerkey="studio.images.list.version">
                ${current.version}
            </rl:column>
            <rl:column headerkey="studio.images.list.arch">
                ${current.arch}
            </rl:column>
            <rl:column headerkey="studio.images.list.type">
                ${current.imageType}
            </rl:column>
        </rl:list>

        <table class="details" align="center">
          <tr>
            <th>Number of VCPUs:</th>
            <td>
              <html:text property="vcpus" value="1" />
            </td>
          <tr>
          <tr>
            <th>Memory (MB):</th>
            <td>
              <html:text property="mem_mb" value="512" />
            </td>
          </tr>
          <tr>
            <th>Bridge Device:</th>
            <td>
              <html:text property="bridge" value="br0" />
            </td>
          </tr>
        </table>

        <div align="right">
            <rhn:submitted/>
            <hr/>
            <input type="submit"
                   name="dispatch"
                   value="Schedule Deployment" />
        </div>
    </rl:listset>

    </body>
</html>
