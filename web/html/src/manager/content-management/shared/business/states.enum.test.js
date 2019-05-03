import statesEnum from "./states.enum";

describe('Testing CLM sources states enum', () => {
  test('findByKey success case', () => {
    expect(statesEnum.findByKey("ATTACHED").description).toBe("added");
    expect(statesEnum.findByKey("BUILT").description).toBe("built");
  });

  test('findByKey default case', () => {
    expect(statesEnum.findByKey("unexisting").description).toBe();
  });

  test('isDeletion testing', () => {
    expect(statesEnum.isDeletion("DETACHED")).toBeTruthy();
    expect(statesEnum.isDeletion("ATTACHED")).toBeFalsy();
    expect(statesEnum.isDeletion("EDITED")).toBeFalsy();
    expect(statesEnum.isDeletion("BUILT")).toBeFalsy();
  });

  test('isEdited testing', () => {
    expect(statesEnum.isEdited("DETACHED")).toBeTruthy();
    expect(statesEnum.isEdited("ATTACHED")).toBeTruthy();
    expect(statesEnum.isEdited("EDITED")).toBeTruthy();
    expect(statesEnum.isEdited("BUILT")).toBeFalsy();
  });

})
