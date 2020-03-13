/* eslint-disable */
"use strict";

const React = require("react");

const CustomDiv = (props) => {
  const width = props.width + props.um;
  const styleClass = Number.isNaN(width) ? null : { width };
  return (
    <div
        style={styleClass}
        className={'customDiv ' + props.className}
        title={props.title}>
      {props.children}
    </div>
  );
}

module.exports = {
    CustomDiv: CustomDiv
}
