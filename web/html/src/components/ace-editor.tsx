import * as React from "react";
import ReactDOM from "react-dom";

type Props = {
  mode: string;
  minLines: number;
  maxLines: number;
  readOnly: boolean;
  onChange?: (value: string) => void;
  className: string;
  id: string;
  content: React.ReactNode;
};

class AceEditor extends React.Component<Props> {
  editor: any = null;

  componentDidMount() {
    const node = ReactDOM.findDOMNode(this.refs.editor) as HTMLElement;
    try {
      this.editor = ace.edit(node);
      this.editor.setTheme("ace/theme/xcode");
      this.editor.getSession().setMode("ace/mode/" + this.props.mode);
      this.editor.setShowPrintMargin(false);
      this.editor.setOptions({ minLines: this.props.minLines });
      this.editor.setOptions({ maxLines: this.props.maxLines });
      this.editor.setReadOnly(this.props.readOnly);

      this.editor.setValue(this.props.content || "", 1);

      this.editor.getSession().on("change", () => {
        this.props.onChange?.(this.editor.getValue());
      });
    } catch (error) {
      Loggerhead.error(
        "Failed to initialize AceEditor, please check if `ace-editor/ace.js` and related dependencies have been imported in your Jade/JSP template"
      );
      Loggerhead.error(error);
    }
  }

  componentDidUpdate(prevProps: Props) {
    if (prevProps.content !== this.props.content && this.editor) {
      this.editor.setValue(this.props.content || "", 1);
    }
  }

  render() {
    return <div ref="editor" className={this.props.className} id={this.props.id} />;
  }
}

export { AceEditor };
