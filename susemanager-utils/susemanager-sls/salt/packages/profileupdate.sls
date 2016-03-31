packages:
  module.run:
    - name: pkg.info_installed
    - kwargs: {
          attr: 'arch,epoch,version,release,install_date',
          errors: report
      }
products:
  module.run:
    - name: pkg.list_products

