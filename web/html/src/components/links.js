// @flow
"use strict";
const React = require("react");

type ChannelAnchorLinkProps = {
  id: string|number,
  newWindow?: boolean
}

const ChannelAnchorLink = (props: ChannelAnchorLinkProps) =>
  <a className="icon-link" href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`}
    target={props.newWindow ? "_blank" : "_self"}>
      <i className="fa fa-link"></i>
  </a>;

type ChannelLinkProps = {
  id: string|number,
  newWindow?: boolean,
  children?: React.Node
}

const ChannelLink = (props: ChannelLinkProps) =>
  <a href={`/rhn/channels/ChannelDetail.do?cid=${props.id}`} target={props.newWindow ? "_blank" : "_self"}>
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

module.exports = {
  ChannelAnchorLink: ChannelAnchorLink,
  ChannelLink: ChannelLink,
  ActionLink: ActionLink,
  SystemLink: SystemLink
}
