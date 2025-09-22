import * as React from "react";

type Props = {
  width?: string;
  progress: number;
  title?: string;
};

const ProgressBar = (props: Props) => {
  const wrapperWidth = props.width ? { width: props.width } : { width: "100%" };
  const progressStyle = { width: props.progress + "%" };
  return (
    <div
      className={"progress-bar-wrapper progress progress-striped " + (props.progress < 100 ? "active" : "")}
      title={props.title}
      style={wrapperWidth}
    >
      <div className="progress-value progress-bar-text">{props.progress}%</div>
      <div className="progress-bar" style={progressStyle}></div>
    </div>
  );
};

export { ProgressBar };
