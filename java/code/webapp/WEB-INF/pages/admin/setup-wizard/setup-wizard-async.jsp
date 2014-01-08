<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<rl:listset name="groupSet">
  <rhn:csrf />
  <rl:list dataset="productsList" emptykey="images.jsp.noimages">
    <rl:column headerkey="images.jsp.name">
      <input type="checkbox" />
    </rl:column>
    <rl:column headerkey="images.jsp.name" filterattr="name">
      <c:out value="${current.name}" />
    </rl:column>
    <rl:column headerkey="images.jsp.arch">
      <c:out value="${current.arch}" />
    </rl:column>
  </rl:list>
</rl:listset>

<div align="right">
  <hr />
  <input type="button"
         value="Synchronize Channels" />
</div>
