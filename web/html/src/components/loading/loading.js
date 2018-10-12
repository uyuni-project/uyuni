"use strict";
const React = require("react");
const Loading = (props) => {
  return (
    <div className="panel-body text-center">
      {
        props.withBorders ?
          <div className="line-separator"></div>
          : null
      }
      <i className='fa fa-spinner fa-spin fa-1-5x'></i>
      <h4>{props.text}</h4>
      {
        props.withBorders ?
          <div className="line-separator"></div>
          : null
      }
    </div>
  )
}
Loading.propTypes = {
  text: React.PropTypes.string.isRequired,
  withBorders: React.PropTypes.bool
};
module.exports = {
  Loading: Loading
}
