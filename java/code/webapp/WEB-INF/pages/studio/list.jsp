<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:html xhtml="true">
    <body>
	<rhn:toolbar base="h1" img="/img/rhn-icon-errata.gif">
        SUSE Studio Images
    </rhn:toolbar>

	<div class="page-summary">
      <p>Please enter SUSE Studio credentials to list your images from SUSE Studio.</p>
	</div>

    <rl:listset name="groupSet">
        <rhn:csrf />
        <rl:list dataset="pageList"
                 name="images"
                 emptykey="studio.images.list.noimages">
            <rl:selectablecolumn value="${current.selectionKey}"
            	selected="${current.selected}"
                disabled="${not current.selectable}"
                styleclass="first-column"/>
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

        <div align="right">
            <rhn:submitted/>
            <hr/>
            <input type="submit"
                   name="dispatch"
                   value="Schedule Download" />
        </div>
    </rl:listset>

    </body>
</html:html>
