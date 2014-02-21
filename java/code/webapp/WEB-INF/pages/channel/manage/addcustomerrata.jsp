<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
    <!-- disables the enter key from submitting the form -->
    <script type="text/javascript" language="JavaScript">
      $(document).ready(function() {
        $(window).keydown(function(event){
          if(event.keyCode == 13) {
            event.preventDefault();
            return false;
          }
        });
      });
    </script>
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>
<BR>

<rl:listset name="errataSet">
<rhn:csrf />
<input type="hidden" name="cid" value="${cid}">



<table class="details" width="80%">
	<tr><bean:message key="channel.manage.errata.custommsg"/><br /><br /></tr>

	 <tr>
  		<th>Package Association:</th>
  		<td>
			   <input type="checkbox" name="assoc_checked"   <c:if test="${assoc_checked}">checked </c:if>  >
			   <bean:message key="channel.manage.errata.packageassocmsg" />
		 </td>
   </tr>

		<c:if test="${selected_channel != null}">
	   		<input type="hidden" name="selected_channel_old"  value="${selected_channel}">
		</c:if>
		<c:if test="${channel_list != null}">

			  <tr> <th width="10%">Channel:</th><td width="40%">
			  <select name="selected_channel">
				    <option value="" >All Custom Channels</option>
				    <optgroup>
			   		<c:forEach var="option" items="${channel_list}">
			   			<c:choose>
			   				<c:when test="${option.baseChannel}">
			   				    </optgroup>
			   					<option value="${option.id}"  <c:if test="${option.selected eq true}">selected = "selected"</c:if>    >${option.name}	</option>
			   					<optgroup>
			   				</c:when>
			   				<c:otherwise>
								<option value="${option.id}"   <c:if test="${option.selected eq true}">selected = "selected"</c:if> >${option.name}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</optgroup>
			  </select>

			  </td>
			   		  <td>
							  <input class="btn btn-default" type="submit" name="dispatch"  value="<bean:message key='frontend.actions.channels.manager.add.viewErrata'/>">
		  			  </td>
		  	     </tr>
		  </c:if>


  </table>
  <br /><br />

   <c:choose>
   		<c:when test="${pageList != null}">

		  <rl:list dataset="pageList" name="errata"   decorator="SelectableDecorator"
		  			emptykey = "channel.manage.errata.noerrata"
		  			filter="com.redhat.rhn.frontend.action.channel.manage.ErrataFilter">
		  		<rl:decorator name="ElaborationDecorator"/>
		  		<rl:decorator name="PageSizeDecorator"/>

				<rl:selectablecolumn value="${current.selectionKey}"
					selected="${current.selected}"/>

				<rl:column sortable="true"
			           headerkey="exportcolumn.errataAdvisoryType"
			           sortattr="advisoryType"
			           styleclass="center"
		           	   headerclass="thin-column">
							        <c:if test="${current.securityAdvisory}">
							            <rhn:icon type="errata-security" title="erratalist.jsp.securityadvisory" />
							        </c:if>
							        <c:if test="${current.bugFix}">
							            <rhn:icon type="errata-bugfix" title="erratalist.jsp.bugadvisory" />
							        </c:if>
							        <c:if test="${current.productEnhancement}">
							            <rhn:icon type="errata-enhance" title="erratalist.jsp.productenhancementadvisory" />
							        </c:if>
				</rl:column>



				<rl:column sortable="true"
				           headerkey="erratalist.jsp.advisory"
				           sortattr="advisory">
		                      <a href="/rhn/errata/details/Details.do?eid=${current.id}">
		                      <c:out value="${current.advisory}"/>
		                      </a>
				</rl:column>

				<rl:column sortable="true"
				           headerkey="erratalist.jsp.synopsis"
				           sortattr="advisorySynopsis">
		                      <c:out value="${current.advisorySynopsis}"/>
				</rl:column>
				<rl:column sortable="true"
				           headerkey="channel.manage.errata.updatedate"
				           sortattr="updateDateObj">
		                      <c:out value="${current.updateDate}"/>
				</rl:column>

			  </rl:list>

  </c:when>
</c:choose>
  		<p align="right">
		    <input class="btn btn-default" type="submit" name="dispatch"  value="<bean:message key='frontend.actions.channels.manager.add.submit'/>"
                <c:choose>
                    <c:when test="${empty pageList}">disabled</c:when>
                </c:choose>
            >
		</p>
     <rhn:submitted/>
</rl:listset>

</body>
</html>

