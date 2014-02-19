<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<script type="text/javascript" src="/rhn/dwr/interface/ProductsRenderer.js"></script>

<p>
    Select SUSE Products below to trigger the synchronization of software channels.
</p>
<div id="products-content">
    <rhn:icon type="spinner"></rhn:icon><span>Loading ...</span>
    <script type="text/javascript">
        ProductsRenderer.renderAsync(makeAjaxCallback("products-content", false));
    </script>
</div>
