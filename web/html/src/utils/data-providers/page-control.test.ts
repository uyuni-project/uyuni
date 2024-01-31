import PageControl from "./page-control";

test("Page control initialization", () => {
  let pageControl = new PageControl(1, 10);

  expect(pageControl).toHaveProperty("page", 1);
  expect(pageControl).toHaveProperty("pageSize", 10);
  expect(pageControl.query).toBeUndefined();
  expect(pageControl.sort).toBeUndefined();

  pageControl = new PageControl(1, 10, "mystring", null, "mycolumn");

  expect(pageControl).toHaveProperty("query", "mystring");
  expect(pageControl).toHaveProperty("sort", { direction: 1, column: "mycolumn" });

  pageControl = new PageControl(1, 10, "mystring", null, "mycolumn", -1);
  expect(pageControl).toHaveProperty("sort", { direction: -1, column: "mycolumn" });
});
