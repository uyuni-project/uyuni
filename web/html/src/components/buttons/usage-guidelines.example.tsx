export default () => (
  <div>
    <h2>Choosing a button</h2>
    <p>
      Every button type has a specific role. Select the appropriate button type based on the screen's context to ensure
      a clear and seamless user experience.
    </p>
    <table className="table table-striped mt-3">
      <thead>
        <tr>
          <th>Variant</th>
          <th>Use Cases</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>
            <strong>Primary</strong>
          </td>
          <td>
            Calls to action (CTAs), key actions, form submissions.
            <p>
              <strong> Example:</strong> "Add", "Create" or "Submit"
            </p>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Default</strong>{" "}
          </td>
          <td>
            Less important actions, alternative options, supporting information and the most commonly used option.
            <p>
              <strong> Example:</strong>"Edit", "Download" or "Cancel"
            </p>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Danger</strong>{" "}
          </td>
          <td>
            Potentially irreversible actions, deletions, cancellations.
            <p>
              <strong> Example:</strong> Deleting any system or data.
            </p>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Only Icon Button</strong>{" "}
          </td>
          <td>
            Only icon buttons are ideal for presenting actions in compact layouts.
            <p>
              <strong>Example:</strong> Table, Cards.
            </p>
            <p>They can be used individually or grouped together.</p>
            <p>
              To enhance clarity, always pair icon buttons with title attribute to give users a brief explanation of
              their function.
            </p>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Tertiary</strong>{" "}
          </td>
          <td>
            <p>
              Tertiary buttons are designed for less prominent actions and should not compete visually with Primary or
              Default buttons.{" "}
            </p>
            <p>Use icon-only tertiary buttons in cards, forms, inline action or components where space is limited.</p>
            <p>
              <strong>Example:</strong> Inline action buttons for editing an email address in a form.
            </p>
          </td>
        </tr>
      </tbody>
    </table>
    <hr></hr>
    <h2 className="mt-5">Best Practices to Write Useful Button Labels</h2>
    <ul className="mt-0">
      <li>Button labels should typically range from 1 to 3 words to maintain clarity and make them easy to scan.</li>
      <li>Ensure labels are action-oriented and provide clear guidance on what the button does.</li>
      <li>
        Avoid Repetition in Bulk Actions: In lists or bulk-action scenarios, avoid redundant labels for buttons acting
        on "selected items."
        <br />
      </li>
    </ul>
  </div>
);
