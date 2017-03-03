<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<div class="sideleg">
  <h4><bean:message key="visualization-legend.jsp.title" /></h4>
  <ul>
    <li><svg><g class='node default stroke-green'><circle r="5"></circle></g></svg><bean:message key="visualization-legend.jsp.checkin" /></li>
    <li><svg><g class='node default stroke-red'><circle r="5"></circle></g></svg><bean:message key="visualization-legend.jsp.notcheckin" /></li>
  </ul>
</div>