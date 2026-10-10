import { IconTag } from "components/icontag";

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
          <IconTag icon="fa-chevron-down" size="md" className="show-on-collapsed" />
          <IconTag icon="fa-chevron-right" size="md" className="hide-on-collapsed" />
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
