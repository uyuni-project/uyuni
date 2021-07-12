import * as React from "react";

type LinkProps = {
  id: string | number;
  newWindow?: boolean;
  children?: React.ReactNode;
  className?: string;
  title?: string;
};

const ChannelAnchorLink = (props: LinkProps) => (
  <a
    className="channel-anchor-link"
    href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}
  >
    <i className="fa fa-link fa-right"></i>
  </a>
);

const ChannelLink = (props: LinkProps) => (
  <a
    href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}
    title={props.title}
  >
    {props.children}
  </a>
);

const ActionLink = (props: LinkProps) => (
  <a
    href={"/rhn/schedule/ActionDetails.do?aid=" + props.id}
    target={props.newWindow ? "_blank" : "_self"}
    className={props.className}
  >
    {props.children}
  </a>
);

const SystemLink = (props: LinkProps) => (
  <a
    href={"/rhn/systems/details/Overview.do?sid=" + props.id}
    target={props.newWindow ? "_blank" : "_self"}
    className={props.className}
  >
    {props.children}
  </a>
);

const ActionChainLink = (props: LinkProps) => (
  <a
    href={"/rhn/schedule/ActionChain.do?id=" + props.id}
    target={props.newWindow ? "_blank" : "_self"}
    className={props.className}
  >
    {props.children}
  </a>
);

const SystemGroupLink = (props: LinkProps) => (
  <a
    href={`/rhn/groups/GroupDetail.do?sgid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}
    className={props.className}
  >
    {props.children}
  </a>
);

export { ChannelAnchorLink, ChannelLink, ActionLink, SystemLink, ActionChainLink, SystemGroupLink };
