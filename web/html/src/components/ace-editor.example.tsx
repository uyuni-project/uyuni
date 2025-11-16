import { AceEditor } from "components/ace-editor";

import exampleContent from "./ace-editor.example.tsx?raw";

export default () => {
  return <AceEditor mode="jsx" content={exampleContent} />;
};
