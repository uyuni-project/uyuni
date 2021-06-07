import * as React from "react";
import { useState, useEffect } from "react";
import { Select, FormContext } from "components/input";
import Network, { JsonResult } from "utils/network";
import { showErrorToastr } from "components/toastr/toastr";
import { Props as FilterFormProps } from "../filter-form";

import { Template } from "./index";

type Product = {
  id: number;
  label: string;
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

function getProductKernels(productId: number): Promise<Kernel[]> {
  return Network.get(`/rhn/manager/api/contentmanagement/livepatching/kernels/${productId}`)
    .then((res: JsonResult<Kernel[]>) => res.success ? res.data : Promise.reject(res))
    .then(res => {
      res[0].latest = true;
      return res;
    });
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
  const [products, setProducts] = useState<Product[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch(res => res.messages?.flatMap(showErrorToastr) || handleResponseErrors(res));
  }, []);

  useEffect(() => {
    if (clientId) {
      //TODO: To be implemented with "Live Patching for a system" template
      throw new Error("Not implemented");
    } else if (productId) {
      getProductKernels(productId).then(result => {
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
    template, // If the template changes, reset what we previously had
    clientId,
    productId,
    setModelValue,
  ]);

  return (
    <>
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
            getOptionLabel={product => product.label}
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
        getOptionLabel={kernel => `${kernel.version}${kernel.latest ? ` (${t("latest")})` : ""}`}
      />
    </>
  );
};
