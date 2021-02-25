import * as React from "react";
import styles from "./build.css";

type Props = {
  id: string;
  text: string;
  collapsed?: boolean;
};

const BuildVersion = ({ id, text, collapsed }: Props) => {
  return (
    <div>
      <dd className="collapsible-content">
        <div
          data-toggle="collapse"
          data-target={`#historyentry_${id}`}
          className={`${styles.version_collapse_line} pointer accordion-toggle collapsed`}
        >
          <i className="fa fa-chevron-down show-on-collapsed fa-small" />
          <i className="fa fa-chevron-right hide-on-collapsed fa-small" />
          <span>{text.split("\n")[0]}</span>
        </div>
        <div className="collapse" id={`historyentry_${id}`}>
          <pre>{text}</pre>
        </div>
      </dd>
    </div>
  );
};

BuildVersion.defaultProps = {
  id: undefined,
  text: undefined,
  collapsed: true,
};

export default BuildVersion;
