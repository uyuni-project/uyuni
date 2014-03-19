<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<script>hideModal();</script>

<rl:listset name="mirrorCredsListSet">
    <rhn:csrf />
    <rl:list name="mirrorCredsList"
             dataset="mirrorCredsList"
             emptykey="mirror-credentials.jsp.empty">
        <rl:column headerkey="mirror-credentials.jsp.email"
                   bound="true"
                   attr="email" />
        <rl:column headerkey="mirror-credentials.jsp.username"
                   bound="true"
                   attr="user" />
        <rl:column headerkey="mirror-credentials.jsp.actions"
                   bound="false">
            <span id="verify-${current.id}">
                <a href="javascript:void(0);" onClick="verifyCredentials('${current.id}', true);">
                    <rhn:icon type="item-verify" title="mirror-credentials.jsp.verify" />
                </a>
                <script>verifyCredentials('${current.id}', false);</script>
            </span>
            <span id="primary-${current.id}">
                <c:if test="${current.id == 0}">
                    <rhn:icon type="item-default" title="mirror-credentials.jsp.primary" />
                </c:if>
                <c:if test="${current.id > 0}">
                    <a href="javascript:void(0);" onCLick="makePrimaryCredentials('${current.id}')">
                        <rhn:icon type="item-make-default" title="mirror-credentials.jsp.make-primary" />
                    </a>
                </c:if>
            </span>
            <span>
                <a href="javascript:void(0);" onCLick="initSubscriptions('${current.id}')" data-toggle="modal" data-target="#modal-list-subscriptions">
                    <rhn:icon type="item-list" title="mirror-credentials.jsp.subscriptions" />
                </a>
            </span>
            <span>
                <a href="javascript:void(0);" onCLick="initEdit('${current.id}','${current.email}','${current.user}')" data-toggle="modal" data-target="#modal-edit-credentials">
                    <rhn:icon type="item-edit" title="mirror-credentials.jsp.edit" />
                </a>
            </span>
            <span id="delete-${current.id}">
                <a href="javascript:void(0);" onClick="initDelete('${current.id}', '${current.email}', '${current.user}');" data-toggle="modal" data-target="#modal-delete-credentials">
                    <rhn:icon type="item-del" title="mirror-credentials.jsp.delete" />
                </a>
            </span>
        </rl:column>
    </rl:list>
</rl:listset>
