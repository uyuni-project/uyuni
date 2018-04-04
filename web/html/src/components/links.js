// @flow
"use strict";
const React = require("react");

type ChannelAnchorLinkProps = {
  id: string|number,
  newWindow?: boolean
}

const ChannelAnchorLink = (props: ChannelAnchorLinkProps) =>
  <a className="channel-anchor-link" href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}>
      <i className="fa fa-link fa-right"></i>
  </a>;

type ChannelLinkProps = {
  id: string|number,
  newWindow?: boolean,
  children?: React.Node,
  title: string
}

const ChannelLink = (props: ChannelLinkProps) =>
  <a href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}
    title={props.title}>
  { props.children }
  </a>;

type ActionLinkProps = {
  id: string|number,
  newWindow?: boolean,
  children?: React.Node
}

const ActionLink = (props: ActionLinkProps) =>
  <a href={"/rhn/schedule/ActionDetails.do?aid=" + props.id} target={props.newWindow ? "_blank" : "_self"}>
  { props.children }
  </a>;

type SystemLinkProps = {
  id: string|number,
  newWindow?: boolean,
  children?: React.Node
}

const SystemLink = (props: SystemLinkProps) =>
  <a href={"/rhn/systems/details/Overview.do?sid=" + props.id} target={props.newWindow ? "_blank" : "_self"}>
  { props.children }
  </a>;

type ActionChainLinkProps = {
  id: string|number,
  newWindow?: boolean,
  children?: React.Node
}

const ActionChainLink = (props: ActionChainLinkProps) =>
  <a href={"/rhn/schedule/ActionChain.do?id=" + props.id} target={props.newWindow ? "_blank" : "_self"}>
  { props.children }
  </a>;

module.exports = {
  ChannelAnchorLink: ChannelAnchorLink,
  ChannelLink: ChannelLink,
  ActionLink: ActionLink,
  SystemLink: SystemLink,
  ActionChainLink: ActionChainLink
}
