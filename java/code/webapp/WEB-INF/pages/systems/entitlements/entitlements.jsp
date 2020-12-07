<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<html>
<head>
</head>
<body>
<rhn:toolbar base="h1" icon="header-system" imgAlt="overview.jsp.alt"
             helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/systems/systems-list.html">
  <bean:message key="systementitlements.jsp.header"/>
</rhn:toolbar>

<p><bean:message key="systementitlements.jsp.para1" /></p>
<p><bean:message key="systementitlements.jsp.para2" /></p>


<html:form action="/systems/SystemEntitlementsSubmit">
    <rhn:csrf />
    <rhn:submitted />

    <rhn:list pageList="${requestScope.pageList}"
                  noDataText="systementitlements.jsp.nodata"
              legend="system">

      <rhn:listdisplay  set="${requestScope.set}"
         filterBy="systemlist.jsp.system"
         domainClass="systems"
         >
        <rhn:set value="${current.id}" disabled="${not current.selectable}"/>
            <rhn:column header="systemlist.jsp.status"
                        style="text-align: left;">
                ${current.statusDisplay}
            </rhn:column>
            <rhn:column header="systemlist.jsp.system"
                        url="/rhn/systems/details/Overview.do?sid=${current.id}">
                ${fn:escapeXml(current.serverName)}
            </rhn:column>

            <rhn:column header="systementitlements.jsp.baseentitlement">
                ${current.baseEntitlementLevel}
            </rhn:column>

            <rhn:column header="systementitlements.jsp.addonentitlement">
                ${current.addOnEntitlementLevel}
            </rhn:column>

            <rhn:column header="systemlist.jsp.channel">
            <c:choose>
                <c:when test="${current.channelId == null}">
                        <bean:message key="none.message"/>
                </c:when>
                <c:otherwise>
                        <a href="/rhn/channels/ChannelDetail.do?cid=${current.channelId}">
                                ${fn:escapeXml(current.channelLabels)}
                        </a>
                </c:otherwise>
        </c:choose>
            </rhn:column>
      </rhn:listdisplay>
    </rhn:list>

<!--  Entitlements Section -->
    <c:if test="${requestScope.showCommands}">
        <hr/>
        <div class="panel panel-default">
                  <table class="table">
                        <!-- On SUSE Manager doing a mass change of base entitlement will not work -->
                        <!-- Base Entitlement Section -REMOVED- (See git history)-->
                        <!--  Add On Entitlement Section -->
                        <tr>
                            <th><bean:message key="systementitlements.jsp.addonentitlement" /></th>
                            <td class="text-right"><html:select property="addOnEntitlement">
                                    <html:optionsCollection name="addOnEntitlements" />
                                </html:select> <html:submit styleClass="btn btn-default" property="dispatch">
                                    <bean:message key="systementitlements.jsp.add_entitlement" />
                                </html:submit> <html:submit styleClass="btn btn-default" property="dispatch">
                                    <bean:message key="systementitlements.jsp.remove_entitlement" />
                                </html:submit></td>
                        </tr>
                  </table>
                </div>
     </c:if>

<!--  Entitlement Counts Section -->
       <h2><bean:message key="systementitlements.jsp.entitlement_counts" /></h2>

<!--  Base Entitlement Counts Section -->
        <div class="panel panel-default">
          <div class="panel-heading">
                  <h5><bean:message key="systementitlements.jsp.base_entitlements"/></h5>
          </div>
          <table class="table">
            <c:forEach items="${baseEntitlementCounts}" var="ent">
              <tr>
                <td><bean:message key="${ent.key}"/>:</td>
                <td class="text-right">${ent.value}</td>
              </tr>
            </c:forEach>
          </table>
        </div>

<!--  Add - On Entitlement Counts Section -->
        <div class="panel panel-default">
          <div class="panel-heading">
            <h5><bean:message key="systementitlements.jsp.addonentitlement"/></h5>
          </div>
          <table class="table">
            <c:forEach items="${addOnEntitlementCounts}" var="ent">
              <tr>
                <td><bean:message key="${ent.key}"/>:</td>
                <td class="text-right">${ent.value}</td>
              </tr>
            </c:forEach>
          </table>
        </div>

<!--  Foot Note -->

</html:form>
</body>
</html>
