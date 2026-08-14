declare module "*.css";
declare module "*.scss";
declare module "*.scss?lazy" {
  const stylesheet: {
    use(): void;
    unuse(): void;
  };
  export default stylesheet;
}
declare module "*.svg";
declare module "*.png";
declare module "*?raw";
