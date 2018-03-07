"use strict";

const React = require("react");

const ProgressBar = (props) => {
  const progressStyle = { width: props.progress + '%' };
  return (
    <div className='progress-bar-wrapper progress progress-striped active'>
        <div className='progress-value progress-bar-text'>{props.progress}%</div>
        <div className='progress-bar' style={progressStyle}></div>
    </div>
  )
}

module.exports = {
    ProgressBar: ProgressBar
}
