<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<rl:listset name="mirrorCredsListSet">
    <rhn:csrf />
    <rl:list name="mirrorCredsList"
             dataset="mirrorCredsList"
             emptykey="mirror-credentials.jsp.empty">
        <rl:column headerkey="mirror-credentials.jsp.user"
                   bound="true"
                   attr="user" />
        <rl:column headerkey="mirror-credentials.jsp.email"
                   bound="true"
                   attr="email" />
        <rl:column headerkey="mirror-credentials.jsp.actions"
                   bound="false">
            <span id="subscriptions-${current.id}">
                <a href="javascript:void(0);" onClick="downloadSubscriptions('${current.id}');">
                    <rhn:icon type="item-cloud-download" title="mirror-credentials.jsp.download" />
                </a>
            </span>
            <a>
                <rhn:icon type="item-edit" title="mirror-credentials.jsp.edit" />
            </a>
            <span id="delete-${current.id}">
                <a href="javascript:void(0);" onClick="deleteCredentials('${current.id}');">
                    <rhn:icon type="item-del" title="mirror-credentials.jsp.delete" />
                </a>
            </span>
        </rl:column>
    </rl:list>
</rl:listset>
