import * as React from "react";
import { useState, useEffect } from "react";
import { Text, Select, FormContext } from "components/input";
import { clmFilterOptions } from "../shared/business/filters.enum";
import { Props as FilterFormProps } from "./filter-form";
import { Cancelable } from "utils/functions";

type Client = {
  id: number;
  name: string;
  kernelVersion?: string;
};

type Kernel = {
  id: number;
  version: string;
};

async function getClients(): Cancelable<Client[]> {
  const PLACEHOLDER_CLIENTS = [
    {
      id: 1,
      name: "Placeholder Client 1",
    },
    {
      id: 2,
      name: "Placeholder Client 2",
    },
    {
      id: 3,
      name: "Placeholder Client 3",
    },
  ];
  return PLACEHOLDER_CLIENTS;
}

async function getKernels(client: any): Cancelable<Kernel[]> {
  console.log("get kernels for", client);
  if (client.id === 1) {
    
  }
  const PLACEHOLDER_KERNELS = [
    {
      id: 234,
      version: "0.1.2",
    },
  ];
  return PLACEHOLDER_KERNELS;
}

export default (props: FilterFormProps) => {
  const filterType = props.filter.type || "";
  if (![clmFilterOptions.LIVE_PATCHING_SYSTEM.key, clmFilterOptions.LIVE_PATCHING_PRODUCT.key].includes(filterType)) {
    return null;
  }
  const formContext = React.useContext(FormContext);
  const client = formContext.model.client;
  const setModelValue = formContext.setModelValue;
  const [clients, setClients] = useState<Client[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getClients().then(clients => setClients(clients));
  }, []);

  useEffect(() => {
    if (client) {
      getKernels(client).then(kernels => setKernels(kernels));
      // TODO: Set according to spec instead
      setModelValue?.("kernel", null);
      // TODO: For project
    } else if (false) {

    } else {
      setKernels([]);
      // formContext.model.kernel = undefined;
    }
  }, [client, setModelValue]);

  return (
    <>
      <Text name={"labelPrefix"} label={t("Prefix")} labelClass="col-md-3" divClass="col-md-6" required />
      {clmFilterOptions.LIVE_PATCHING_SYSTEM.key === filterType && (
        <>
          <Select
            name="client"
            label={t("Client")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            disabled={props.editing}
            options={clients}
            getOptionValue={client => client.id}
            getOptionLabel={client => client.name}
          />
        </>
      )}
      <Select
        name="kernel"
        label={t("Kernel")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required={!!client}
        disabled={props.editing || !client}
        options={kernels}
        getOptionValue={kernel => kernel.id}
        getOptionLabel={kernel => kernel.version}
      />
    </>
  );
};
