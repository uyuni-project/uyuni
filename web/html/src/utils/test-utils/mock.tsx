import * as React from "react";

// Mock the datetime picker to avoid it causing issues due to missing jQuery/Bootstrap parts
jest.mock("components/datetimepicker", () => {
  return {
    __esModule: true,
    DateTimePicker: () => {
      return <div>DateTimePicker mockup</div>;
    },
  };
});
