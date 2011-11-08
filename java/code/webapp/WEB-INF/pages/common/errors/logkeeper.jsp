<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html:xhtml />
<page:applyDecorator name="layout_c">
<body>
    <h1><img src="/img/rhn-icon-warning.gif" /> We're Sorry!</h1>

    <p>SUSE Manager was unable to connect to the 'auditlog-keeper' service, which prevented your request
    from being processed. It is currently not possible to execute this action, please try again later.</p>

    <p>The SUSE Manager administrator has already been contacted with details about the problem.</p>
</body>
</page:applyDecorator>
