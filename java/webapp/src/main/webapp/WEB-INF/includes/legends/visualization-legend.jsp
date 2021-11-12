<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<div class="sideleg">
  <h4><bean:message key="visualization-legend.jsp.title" /></h4>
  <ul>
    <li>
      <svg>
        <g class="node root" >
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-suse-manager"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.root" />
    </li>
    <li>
      <svg>
        <g class="node vhm">
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-virtual-host-manager"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.vhm" />
    </li>
    <li>
      <svg>
        <g class="node inner-node" >
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-desktop-filled-group"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.group" />
    </li>
    <li>
      <svg>
        <g class="node system">
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-desktop-filled"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.system" />
    </li>
    <li>
      <svg>
        <g class="node system stroke-red non-checking-in">
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-desktop-filled"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.notcheckin" />
    </li>
    <li>
      <svg>
        <g class="node system stroke-red unpatched">
          <foreignObject width="1.1em" height="1.1em">
            <i class="fa spacewalk-icon-desktop-filled"></i>
          </foreignObject>
        </g>
      </svg>
      <bean:message key="visualization-legend.jsp.notpatched" />
    </li>
  </ul>
</div>
