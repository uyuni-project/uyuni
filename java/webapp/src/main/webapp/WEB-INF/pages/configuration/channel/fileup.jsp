<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
    <body>
        <%@ include file="/WEB-INF/pages/common/fragments/configuration/channel/details-header.jspf"%>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><bean:message key="addfiles.jsp.upload-link" /> </h4>
            </div>
            <div class="panel-body">
                <!-- Upload file to channel  -->
                <html:form
                    action="/configuration/ChannelUploadFiles.do?ccid=${ccid}&csrf_token=${csrfToken}"
                    styleClass="form-horizontal"
                    enctype="multipart/form-data">
                    <rhn:csrf />
                    <rhn:submitted />
                    <rhn:require acl="config_channel_type(state)" mixins="com.redhat.rhn.common.security.acl.ConfigAclHandler">
                        <%@ include file="/WEB-INF/pages/common/fragments/configuration/channel/upload-defaultperms.jspf" %>
                    </rhn:require>
                    <rhn:require acl="not config_channel_type(state)" mixins="com.redhat.rhn.common.security.acl.ConfigAclHandler">
                        <%@ include file="/WEB-INF/pages/common/fragments/configuration/channel/upload.jspf" %>
                    </rhn:require>
                </html:form>
            </div>
        </div>
    </body>
</html>
