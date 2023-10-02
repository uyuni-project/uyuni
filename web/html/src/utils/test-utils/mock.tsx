// Mock the datetime picker to avoid it causing issues due to missing jQuery/Bootstrap parts
jest.mock("components/datetime/DateTimePicker", () => {
  return {
    __esModule: true,
    DateTimePicker: () => {
      return <div>DateTimePicker mockup</div>;
    },
  };
});

// Mock the ACE Editor to avoid it causing issues due to the missing proper library import
jest.mock("components/ace-editor", () => {
  return {
    __esModule: true,
    AceEditor: () => {
      return <div>My AceEditor mockup</div>;
    },
  };
});

export {};
