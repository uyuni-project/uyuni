import { FieldInputProps, useField } from "formik";

import { Field } from "../field";

// TODO: These are just sample items I came up with, put whatever data types you actually need here
type ItemType =
  | {
      type: "tree";
      value: AnsibleTree;
    }
  | {
      type: "text";
      value: string;
    }
  | {
      type: "boolean";
      value: boolean;
    };

export type AnsibleTree = Record<string, ItemType>;

// TODO: This is just a placeholder, I don't know how we actually name things in your context
let nameCounter = 0;

export const AnsibleTreeDemo = (props: FieldInputProps<AnsibleTree>) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [field, _, helper] = useField<AnsibleTree>(props.name);

  const addField = (item: ItemType) => {
    const newName = `random-name-${nameCounter++}`;
    const newTree: AnsibleTree = {
      ...field.value,
      [newName]: item,
    };
    helper.setValue(newTree);
  };

  return (
    // TODO: These are just placeholder styles to show the structure, remove them for the actual implementation
    <div style={{ paddingLeft: "10px", border: "1px solid #ccc", width: "100%" }}>
      {Object.entries(field.value).map(([key, item]) => {
        const name = `${props.name}.${key}.value`;
        if (item.type === "text") {
          return <Field key={key} name={name} label={key} />;
        }
        if (item.type === "boolean") {
          return <Field key={key} name={name} label={key} as={Field.Check} />;
        }
        if (item.type === "tree") {
          return <Field key={key} name={name} label={key} as={AnsibleTreeDemo} />;
        }
        throw new RangeError(`Got no renderer for FieldTree type "${(item as any).type}"`);
      })}

      <div>
        <button onClick={() => addField({ type: "text", value: "" })} type="button">
          add text
        </button>
        <button onClick={() => addField({ type: "boolean", value: false })} type="button">
          add boolean
        </button>
        <button onClick={() => addField({ type: "tree", value: {} })} type="button">
          add subtree
        </button>
      </div>
    </div>
  );
};
