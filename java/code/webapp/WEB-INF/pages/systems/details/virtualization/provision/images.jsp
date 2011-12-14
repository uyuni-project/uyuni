<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:html xhtml="true">
    <body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
    <br/>
	<rhn:toolbar base="h1" img="/img/rhn-icon-errata.gif">
        Image Deployment
    </rhn:toolbar>

	<div class="page-summary">
      <p>Please choose one of these images.</p>
	</dpiv>
	
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

        <p>VCpus: <input type="text" name="vcpus" value="1" /></p>
        <p>MemKb: <input type="text" name="memkb" value="524288" /></p>

        <div align="right">
            <rhn:submitted/>
            <hr/>
            <input type="submit"
                   name="dispatch"
                   value="Deploy Image" />
        </div>
    </rl:listset>

    </body>
</html:html>
