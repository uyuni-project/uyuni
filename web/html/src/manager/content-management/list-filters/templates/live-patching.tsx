import * as React from "react";
import { useState, useEffect, useRef } from "react";
import { Select, FormContext } from "components/input";
import Network, { JsonResult } from "utils/network";
import { showErrorToastr } from "components/toastr/toastr";
import { Props as FilterFormProps } from "../filter-form";

import { Template } from "./index";

type Product = {
  id: number;
  label: string;
};

type System = {
  id: number;
  name: string;
  kernel: string;
};

type Kernel = {
  id: number;
  version: string;
  latest?: boolean;
};

function handleResponseErrors(res) {
  Network.responseErrorMessage(res)
    .map(msg => showErrorToastr(msg.text));
}

function getProducts(): Promise<Product[]> {
  return Network.get("/rhn/manager/api/contentmanagement/livepatching/products")
    .then((res: JsonResult<Product[]>) => res.success ? res.data : Promise.reject(res));
}

function getSystems(query: string): Promise<System[]> {
  return Network.get(`/rhn/manager/api/contentmanagement/livepatching/systems?q=${query}`)
    .then((res: JsonResult<System[]>) => res.success ? res.data : Promise.reject(res));
}

function getKernels(id: number, type: string): Promise<Kernel[]> {
  return Network.get(`/rhn/manager/api/contentmanagement/livepatching/kernels/${type}/${id}`)
    .then((res: JsonResult<Kernel[]>) => res.success ? res.data : Promise.reject(res))
    .then(res => {
      if (res.length > 0)
        res[0].latest = true;
      return res;
    });
}

function usePrevious<T>(value: T) {
  const ref = useRef<T>();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
}

export default (props: FilterFormProps & { template: Template }) => {
  const template = props.template;
  const prevTemplate = usePrevious(template);
  if (!template) {
    return null;
  }

  const formContext = React.useContext(FormContext);
  const setModelValue = formContext.setModelValue;
  const { productId, systemId, systemName, kernelName } = formContext.model;
  const [products, setProducts] = useState<Product[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch(res => res.messages?.flatMap(showErrorToastr) || handleResponseErrors(res));
  }, []);

  useEffect(() => {
    // If the template changes, reset what we previously had
    if (prevTemplate) {
      setModelValue?.("systemId", null);
      setModelValue?.("productId", null);
      setModelValue?.("kernelId", null);
      setKernels([]);
    }
  }, [template]);

  useEffect(() => {
    if (systemId || productId) {
      getKernels(systemId ?? productId, systemId ? "system" : "product").then(result => {
        setKernels(result);

        const latestKernel = result.find(item => Boolean(item.latest));
        const kernelId = latestKernel?.id ?? result[0]?.id ?? null;
        setModelValue?.("kernelId", kernelId);
      })
        .catch(res => res.messages?.flatMap(showErrorToastr) || handleResponseErrors(res));
    } else {
      setKernels([]);
      setModelValue?.("kernelId", null);
    }
  }, [
    systemId,
    productId,
  ]);

  // Are we using predefined values from the URL params?
  const hasInitialValues = Boolean(systemId && systemName && kernelName);
  const defaultValueOption = hasInitialValues ? {
    id: systemId,
    name: systemName,
    kernel: kernelName,
  } : undefined;

  return (
    <>
      {template === Template.LivePatchingProduct && (
        <>
          <Select
            name="productId"
            label={t("Product")}
            labelClass="col-md-3"
            divClass="col-md-6"
            options={products}
            getOptionValue={product => product.id}
            getOptionLabel={product => product.label}
          />
        </>
      )}
      {template === Template.LivePatchingSystem && (
        <>
          <Select
            loadOptions={getSystems}
            name="systemId"
            label={t("System")}
            labelClass="col-md-3"
            divClass="col-md-6"
            getOptionValue={system => system.id}
            getOptionLabel={system => `${system.name} (${system.kernel})`}
            defaultValueOption={defaultValueOption}
          />
        </>
      )}
      <Select
        name="kernelId"
        label={t("Kernel")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required={!!(systemId || productId)}
        disabled={!systemId && !productId}
        options={kernels}
        getOptionValue={kernel => kernel.id}
        getOptionLabel={kernel => `${kernel.version}${kernel.latest ? ` (${t("latest")})` : ""}`}
      />
    </>
  );
};
