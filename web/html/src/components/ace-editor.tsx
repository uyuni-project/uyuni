import * as React from "react";
import ReactDOM from "react-dom";

type Props = {
  mode: string;
  minLines: number;
  maxLines: number;
  readOnly: boolean;
  onChange?: (...args: any[]) => any;
  className: string;
  id: string;
  content: React.ReactNode;
};

class AceEditor extends React.Component<Props> {
  componentDidMount() {
    const node = ReactDOM.findDOMNode(this.refs.editor);
    try {
      const editor = ace.edit(node);
      editor.setTheme("ace/theme/xcode");
      editor.getSession().setMode("ace/mode/" + this.props.mode);
      editor.setShowPrintMargin(false);
      editor.setOptions({ minLines: this.props.minLines });
      editor.setOptions({ maxLines: this.props.maxLines });
      editor.setReadOnly(this.props.readOnly);

      editor.getSession().on("change", () => {
        this.props.onChange?.(editor.getSession().getValue());
      });
    } catch (error) {
      Loggerhead.error(
        "Failed to initialize AceEditor, please check if `ace-editor/ace.js` and related dependencies have been imported in your Jade/JSP template"
      );
      Loggerhead.error(error);
    }
  }

  render() {
    return (
      <div ref="editor" className={this.props.className} id={this.props.id}>
        {this.props.content}
      </div>
    );
  }
}

export { AceEditor };
