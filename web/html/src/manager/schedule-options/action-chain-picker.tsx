import { useEffect, useState } from "react";

import { Combobox } from "components/combobox";

type ActionChain = {
  id: string;
  text: string;
};

type Props = {
  actionChains: ActionChain[];
};

export const ActionChainPicker = (props: Props) => {
  const [selectedId, setSelectedId] = useState(props.actionChains[0]?.id || "");

  useEffect(() => {
    setInteropValue(selectedId);
  }, []);

  // This module needs to interface with legacy fields in schedule-options.jspf
  const setInteropValue = (value: string) => {
    document.getElementById("action-chain")?.setAttribute("value", value);
  };

  return (
    <Combobox
      options={props.actionChains}
      selectedId={selectedId}
      getNewOptionData={(input, label) => {
        const sanitized = input.replace(/[',]/g, "");
        const maxLength = 256;
        const cut = sanitized.substring(0, maxLength);
        return { id: cut, value: cut, label };
      }}
      onSelect={(item) => {
        setInteropValue(item.id);
        setSelectedId(item.id);
      }}
      onFocus={() => {
        document.getElementById("schedule-by-action-chain")?.setAttribute("checked", "checked");
      }}
    />
  );
};
