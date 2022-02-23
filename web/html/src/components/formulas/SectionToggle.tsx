import * as React from "react";

type Props = {
  index?: any;
  isVisible: (index: any) => boolean;
  setVisible: (index: any, isVisible: boolean) => any;
  children?: React.ReactNode;
};

const SectionToggle = (props: Props) => {
  const toggleSection = () => {
    let visible = props.isVisible(props.index);
    if (visible === undefined) {
      visible = true; // initially is open
    }
    props.setVisible(props.index, !visible);
  };

  return (
    <span onClick={() => toggleSection()}>
      <i
        className={"fa " + (props.isVisible(props.index) ? "fa-angle-up" : "fa-angle-down")}
        title={t("Toggle section visibility")}
      />
      {props.children}
    </span>
  );
};

export default SectionToggle;
