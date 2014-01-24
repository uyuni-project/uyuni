<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<rl:listset name="productsListSet">
    <rhn:csrf />
    <rhn:submitted />
    <rl:list name="productsList"
             dataset="productsList"
             emptykey="suse-products.jsp.noproducts">
        <rl:selectablecolumn value="${current.ident}"
                             selected="${current.selected}"
                             disabled="${not current.selectable}" />
        <rl:column headerkey="suse-products.jsp.name"
                   bound="true"
                   attr="name" />
        <rl:column headerkey="suse-products.jsp.arch"
                   bound="true"
                   attr="arch" />
    </rl:list>

    <div class="pull-right">
        <hr />
        <html:submit property="dispatch"
                     styleClass="btn btn-success">
            <bean:message key="suse-products.jsp.dispatch" />
        </html:submit>
    </div>
</rl:listset>
