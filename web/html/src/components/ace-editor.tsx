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
    const component = this;

    const node = ReactDOM.findDOMNode(component.refs.editor);
    const editor = ace.edit(node);
    editor.setTheme("ace/theme/xcode");
    editor.getSession().setMode("ace/mode/" + component.props.mode);
    editor.setShowPrintMargin(false);
    editor.setOptions({ minLines: component.props.minLines });
    editor.setOptions({ maxLines: component.props.maxLines });
    editor.setReadOnly(component.props.readOnly);

    editor.getSession().on("change", function () {
      component.props.onChange?.(editor.getSession().getValue());
    });
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
