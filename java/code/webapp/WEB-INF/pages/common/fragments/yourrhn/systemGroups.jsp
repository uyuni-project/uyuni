<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <rl:listset name="systemGroupsSet">
        <rhn:csrf />
                <rl:list dataset="systemGroupList"
                 width="100%"
                 name="systemsGroupList"
                 title="${rhn:localize('yourrhn.jsp.systemgroups.header')}"
                 styleclass="list list-doubleheader"
                 hidepagenums="true"
                 emptykey="yourrhn.jsp.systemgroups.none">

                        <rl:column headerkey="yourrhn.jsp.systemgroups">
                <a class="js-spa" href="/rhn/groups/GroupDetail.do?sgid=${current.id}">
                <c:out value="${current.name}"/></a>
            </rl:column>

                <rl:column headerkey="grouplist.jsp.systems">
                <a class="js-spa" href="/rhn/groups/GroupDetail.do?sgid=${current.id}">
                <c:out value="${current.serverCount}"/></a>
                </rl:column>

                </rl:list>

                  <a class="js-spa" href="/rhn/systems/SystemGroupList.do">
                        <div class="btn btn-default spacewalk-btn-margin-vertical"><rhn:icon type="header-system-groups" /><bean:message key="yourrhn.jsp.allgroups" /></div>
                </a>

        </rl:listset>
