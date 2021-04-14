import * as React from "react";
import { useState, useEffect } from "react";
import { Text, Select, FormContext } from "components/input";
import { Props as FilterFormProps } from "../filter-form";
import { Cancelable } from "utils/functions";

import { Template } from "./index";

type Client = {
  id: number;
  name: string;
  kernelId?: string;
};

type Product = {
  id: number;
  name: string;
};

type Kernel = {
  id: number;
  version: string;
};

async function getClients(): Cancelable<Client[]> {
  return [
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
}

async function getProducts(): Cancelable<Product[]> {
  return [
    {
      id: 1,
      name: "SLES product 1",
    },
  ];
}

// TODO: Specify where and how this data realistically comes from
async function getKernels(...args: any[]): Cancelable<Kernel[]> {
  return [
    {
      id: 345,
      version: "1.2.3",
    },
  ];
}

export default (props: FilterFormProps & { template: Template }) => {
  const template = props.template;
  if (!template) {
    return null;
  }

  const formContext = React.useContext(FormContext);
  const setModelValue = formContext.setModelValue;
  const clientId = formContext.model.clientId;
  const productId = formContext.model.productId;
  const [clients, setClients] = useState<Client[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getClients().then(result => setClients(result));
    getProducts().then(result => setProducts(result));
  }, []);

  useEffect(() => {
    if (clientId) {
      getKernels(clientId).then(result => {
        setKernels(result);

        const client = clients.find(item => item.id === clientId);
        const kernelId = client?.kernelId ?? null;
        setModelValue?.("kernel", kernelId);
      });
    } else if (productId) {
      getKernels(productId).then(result => {
        setKernels(result);

        const product = products.find(item => item.id === productId);
        // TODO: Specify where and how this data realistically comes from
        setModelValue?.("kernel", null);
      });
    } else {
      setKernels([]);
      setModelValue?.("kernel", null);
    }
  }, [clients, clientId, products, productId, setModelValue]);

  return (
    <>
      <Text name={"labelPrefix"} label={t("Prefix")} labelClass="col-md-3" divClass="col-md-6" required />
      {template === Template.LivePatchingSystem && (
        <>
          <Select
            name="clientId"
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
      {template === Template.LivePatchingProduct && (
        <>
          <Select
            name="productId"
            label={t("Product")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            disabled={props.editing}
            options={products}
            getOptionValue={product => product.id}
            getOptionLabel={product => product.name}
          />
        </>
      )}
      <Select
        name="kernelId"
        label={t("Kernel")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required={!!(clientId || productId)}
        disabled={!clientId && !productId}
        options={kernels}
        getOptionValue={kernel => kernel.id}
        getOptionLabel={kernel => kernel.version}
      />
    </>
  );
};
