<%--
SPDX-FileCopyrightText: 2026 Red Hat, Inc.
SPDX-FileCopyrightText: 2026 SUSE LLC

SPDX-License-Identifier: GPL-2.0-only
--%>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<page:applyDecorator name="layout_error">
<body>
    <h1>
      <rhn:icon type="system-warn" title="500.jsp.imgAlt" />
      <bean:message key="500.jsp.title"/>
    </h1>
    <p><bean:message key="500.jsp.summary"/></p>
    <p><bean:message key="500.jsp.message"/></p>
</body>
</page:applyDecorator>
