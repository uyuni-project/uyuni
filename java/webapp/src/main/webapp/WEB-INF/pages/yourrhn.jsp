<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<html>
<body>

<c:set var="cb_version" value="${rhn:getConfig('web.buildtimestamp')}" />

<rhn:toolbar base="h1" icon="header-taskomatic" imgAlt="yourrhn.jsp.toolbar.img.alt"
             helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/home/home-overview.html">
  <bean:message key="yourrhn.jsp.toolbar"/>
</rhn:toolbar>

<rhn:require acl="is(yourrhn.debug.enabled);">
<a href="/rhn/YourRhnClips.do"> YourRhn Clips Debug Link</a>
</rhn:require>

<c:choose>
  <c:when test="${requestScope.anyListsSelected == 'true'}">
    <div class="row">
      <c:if test="${requestScope.subscriptionWarning == 'y'}">
      <div id="subscription-warning" class="col-md-12">
        <script type="text/javascript">
          ajax("subscription-warning", "", makeRendererHandler("subscription-warning", false).callback);
        </script>
      </div>
      </c:if>
    </div>
    <div class="row">
      <c:if test="${requestScope.tasks == 'y'}">
      <div class="col-md-6" id="tasks-pane" >
        <script type="text/javascript">
          ajax("tasks", "", makeRendererHandler("tasks-pane", false).callback, "text/html")
        </script>
      </div>
      </c:if>
      <c:if test="${requestScope.inactiveSystems == 'y'}">
      <div class="col-md-6" id="inactive-systems-pane" >
        <script type="text/javascript">
          ajax("inactive-systems", "", makeRendererHandler("inactive-systems-pane", false).callback)
        </script>
      </div>
      </c:if>
    </div>
    <div class="row">
      <c:if test="${requestScope.criticalSystems == 'y'}">
      <div id="critical-systems-pane" class="col-md-12">
        <script type="text/javascript">
          ajax("critical-systems", "", makeRendererHandler("critical-systems-pane", false).callback, "text/html")
        </script>
      </div>
      </c:if>
      <c:if test="${requestScope.pendingActions =='y'}">
      <div id="pending-actions-pane" class="col-md-12">
        <script type="text/javascript">
          ajax("pending-actions", "", makeRendererHandler("pending-actions-pane", false).callback)
        </script>
      </div>
      </c:if>
      <c:if test="${requestScope.latestErrata == 'y'}">
      <div id="latest-errata-pane" class="col-md-12">
        <script type="text/javascript">
          ajax("latest-errata", "", makeRendererHandler("latest-errata-pane", false).callback)
        </script>
      </div>
      </c:if>
      <c:if test="${requestScope.systemGroupsWidget == 'y'}">
      <div id="systems-groups-pane" class="col-md-12">
        <script type="text/javascript">
          ajax("systems-groups", "", makeRendererHandler("systems-groups-pane", false).callback, "text/html")
        </script>
      </div>
      </c:if>
      <c:if test="${requestScope.recentlyRegisteredSystems == 'y'}">
      <div id="recently-registered-pane" class="col-md-12">
        <script type="text/javascript">
          ajax("recent-systems", "", makeRendererHandler("recently-registered-pane", false).callback, "text/html")
        </script>
      </div>
      </c:if>
    </div>
  </c:when>

  <c:otherwise>
    <bean:message key="yourrhn.jsp.nolists" />
  </c:otherwise>
</c:choose>

</body>
</html>
