import * as React from "react";
import { storiesOf } from "@storybook/react";
import { Tree } from "./tree";

const simpleTreeData = {
  rootId: "2",
  items: [
    { id: "2", children: ["3", "4", "8"] },
    { id: "3", data: { key: "three", description: "I am #3" } },
    { id: "4", data: { key: "four", description: "I am #4" }, children: ["5", "6", "7"] },
    { id: "5", data: { key: "five", description: "I am #5" } },
    { id: "6", data: { key: "six", description: "I am #6" }, children: ["7"] },
    { id: "7", data: { key: "seven", description: "I am #7" } },
    { id: "8", data: { key: "eight", description: "I am #8" } },
  ],
};

storiesOf("Tree component", module)
  .add("simple data", () => (
    <Tree
      header={<div>Simple Data</div>}
      data={simpleTreeData}
      renderItem={(item, renderNameColumn) => {
        return (
          <>
            {renderNameColumn(<>Name Column: {item.data.key}</>)}
            <div>{item.data.description}</div>
          </>
        );
      }}
    />
  ))
  .add("tree with selection and initially expanded", () => (
    <Tree
      header={<div>Tree with selection and initially expanded</div>}
      data={simpleTreeData}
      initiallyExpanded={["4"]}
      initiallySelected={["6"]}
      onItemSelectionChanged={(item, checked) => alert(`checkbox changed to ${checked} :  ${JSON.stringify(item)}`)}
      renderItem={(item, renderNameColumn) => {
        return (
          <>
            {renderNameColumn(<>Name Column: {item.data.key}</>)}
            <div>{item.data.description}</div>
          </>
        );
      }}
    />
  ));
