<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"
	prefix="html"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<body>
<rhn:toolbar base="h1" icon="fa-cog"
	creationUrl="ProbeSuiteProbeCreate.do?suite_id=${suite_id}"
	creationType="probe"
	helpUrl="/rhn/help/reference/en-US/s1-sm-monitor.jsp#s2-sm-monitor-psuites">
	<bean:message key="probes.jsp.header1" arg0="${probeSuite.suiteName}" />
</rhn:toolbar>

<rhn:dialogmenu mindepth="0" maxdepth="1"
	definition="/WEB-INF/nav/probesuite_detail_edit.xml"
	renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

<h2><bean:message key="probes.jsp.header2" /></h2>

<div>


<rl:listset name="probeSet">
<rhn:csrf />


<html:hidden property="suite_id" value="${suite_id}" />

   <rl:list emptykey="probes.jsp.noprobes">
		<rl:decorator name="ElaborationDecorator"/>
		<rl:decorator name="PageSizeDecorator"/>

		<rl:selectablecolumn value="${current.id}"
							selected="${current.selected}"/>

                <rl:column sortable="true"
                           bound="false"
                           headerkey="probes.jsp.probe_description"
                           sortattr="description"
                           defaultsort="asc"
                           filterattr="description">
                    <a href="ProbeSuiteProbeEdit.do?suite_id=${suite_id}&probe_id=${current.id}">
					<c:out value="${current.description}"/>
                    </a>

                </rl:column>


               <rl:column sortable="true"
                                   bound="true"
                           headerkey="probes.jsp.command_description"
                           sortattr="cmd_description"
                           attr="cmd_description"  />



  </rl:list>


  <div class="text-right">
   <rhn:submitted/>
   <hr/>

    <input type="submit"
	name ="dispatch"
	    value='<bean:message key="probes.jsp.deleteprobe"/>'/>
	</div>

</rl:listset>






</p>
</div>


</body>
</html>

