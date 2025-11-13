import * as React from "react";

import styles from "./build-version.module.scss";

type Props = {
  id: string;
  text: string;
};

const BuildVersion = ({ id, text }: Props) => {
  return (
    <div>
      <dd className="collapsible-content">
        <div
          data-bs-toggle="collapse"
          data-bs-target={`#historyentry_${id}`}
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

export default BuildVersion;
