import { TableFilter } from "components/table/TableFilter";

const virtualSystemsListOptions = [
  { value: "host_server_name", label: t("Virtual Host") },
  { value: "server_name", label: t("Virtual System") },
];

export const VirtualSystemsListFilter = (props) => {
  return <TableFilter filterOptions={virtualSystemsListOptions} name="criteria" {...props} />;
};
