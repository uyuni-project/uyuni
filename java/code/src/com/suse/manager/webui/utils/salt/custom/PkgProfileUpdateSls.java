package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;
import com.suse.manager.webui.utils.salt.Zypper;
import com.suse.salt.netapi.calls.modules.Pkg;

import java.util.List;
import java.util.Map;

public class PkgProfileUpdateSls {

   @SerializedName("module_|-products_|-pkg.list_products_|-run")
   private StateApplyResult<Ret<List<Zypper.ProductInfo>>> listProducts;

   @SerializedName("module_|-packages_|-pkg.info_installed_|-run")
   private StateApplyResult<Ret<Map<String, Pkg.Info>>> infoInstalled;

   public StateApplyResult<Ret<List<Zypper.ProductInfo>>> getListProducts() {
      return listProducts;
   }

   public StateApplyResult<Ret<Map<String, Pkg.Info>>> getInfoInstalled() {
      return infoInstalled;
   }
}
