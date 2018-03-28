"use strict";

const React = require("react");

const CustomDiv = (props) => {
  const styleClass = { width : props.width + props.um };
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
