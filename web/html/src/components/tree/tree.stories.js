import React from 'react';
import { storiesOf } from '@storybook/react';
import { Tree } from './tree';

storiesOf('Tree component', module)
  .add('simple data', () => (
    <Tree
      header={<h1>Testing</h1>}
      data={{
        rootId: "2",
        items: [
          {id: "2", children: ['3', '4']},
          {id: "3", data: {key: "three", description: "I am 3"}},
          {id: "4", data: {key: "for", description: "I am 4 children"}, children: ["5", "6"]},
          {id: "5", data: {key: "five", description: "I am 5 children"}},
          {id: "6", data: {key: "six", description: "I am 6 children"}}
        ]
      }}
      renderItem={(item, renderNameColumn) => {
        return (
          <>
            Name Column:
            {renderNameColumn(item.data.key)}
            <div>{item.data.description}</div>
          </>
        );
      }}
    />
  ))
