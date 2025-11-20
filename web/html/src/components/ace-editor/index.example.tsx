import { AceEditor } from "components/ace-editor";

import exampleContent from "./index.example.tsx?raw";

export default () => {
  return <AceEditor mode="jsx" content={exampleContent} />;
};
