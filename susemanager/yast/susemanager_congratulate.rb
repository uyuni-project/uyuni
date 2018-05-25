# encoding: utf-8

module Yast
  class SusemanagerCongratulateClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "FileUtils"
      Yast.import "Hostname"
      Yast.import "Wizard"

      @dir = Directory.tmpdir
      @ret = :next

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"
      @product_doc_url = "https://www.suse.com/documentation/suse-manager/"
      if @product_name == "Uyuni"
          @product_doc_url = "https://www.uyuni-project.org/"
      end
      return deep_copy(@ret) if @migration

      @contents = HBox(
        HSpacing(1),
        VBox(
          VSpacing(2),
          # richtext label
          RichText(
            Id(:rt),
            Builtins.sformat(
              _(
                "<p>#{@product_name} Setup is now complete.</p><br>\n" +
                  "<p>Visit <b>https://%1</b> to create the #{@product_name} administrator account.</p>\n" +
                  "<p>For more information, refer to the #{@product_name} Installation and Troubleshooting guide or see <b>#{@product_doc_url}</b>.</p>"
              ),
              Hostname.CurrentFQ
            )
          ),
          VSpacing(2)
        ),
        HSpacing(1)
      )

      @help_text = ""

      # dialog caption
      Wizard.SetContents(
        _("Setup Completed"),
        @contents,
        @help_text,
        true,
        true
      )
      Wizard.DisableBackButton
      Wizard.DisableAbortButton

      UI.UserInput
      deep_copy(@ret)
    end
  end
end

Yast::SusemanagerCongratulateClient.new.main
