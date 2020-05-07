import React, {useState} from 'react';
import {Form} from './Form';
import Combo from './Combo';

export default {
  component: Combo,
  title: 'Forms/Combo'
};

export const Example = () => {
  const [model, setModel] = useState({});

  const options = [
    { value: 'chocolate', label: 'Chocolate' },
    { value: 'strawberry', label: 'Strawberry' },
    { value: 'vanilla', label: 'Vanilla' }
  ];

  return (
    <div className="panel panel-default">
      <div className="panel-body">
        <Form
          model={model}
          onChange={setModel}
          divClass="col-md-12"
          formDirection="form-horizontal"
        >
          <Combo
            name="flavor"
            label={t("Flavor")}
            options={options}
            placeholder={t("Start typing...")}
            emptyText={t("No flavors")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
          />
        </Form>
      </div>
    </div>
  );
}

