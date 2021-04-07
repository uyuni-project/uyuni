import * as React from "react";
import { useState, useEffect } from "react";
import { Text, Select, FormContext } from "components/input";
import { clmFilterOptions } from "../shared/business/filters.enum";
import { Props as FilterFormProps } from "./filter-form";
import { Cancelable, Utils } from "utils/functions";

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
      id: 123,
      name: "Placeholder Client",
    },
  ];
  return PLACEHOLDER_CLIENTS;
}

async function getKernels(client: unknown): Cancelable<Kernel[]> {
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
  const [clients, setClients] = useState<Client[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getClients().then(clients => setClients(clients));
  }, []);

  Utils.useCancelableEffect(() => {
    const request = getKernels(client);
    request.then(kernels => setKernels(kernels));
    return request;
  }, [client]);

  useEffect(() => {
    let request: Cancelable<any> | undefined;
    (async function foo() {
      if (client) {
        request = getKernels(client);
        const kernels = await request;
        setKernels(kernels);
      } else {
        setKernels([]);
      }
    })();
    return () => {
      request?.cancel();
    };
  }, [client]);

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
        required
        disabled={props.editing}
        options={kernels}
        getOptionValue={kernel => kernel.id}
        getOptionLabel={kernel => kernel.version}
      />
    </>
  );
};
