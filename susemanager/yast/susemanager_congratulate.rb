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
                "<p>SUSE Manager Setup is now complete.</p><br>\n" +
                  "<p>Use <b>mgr-sync</b> to add repositories.</p>\n" +
                  "<p>Visit <b>https://%1</b> to create the SUSE Manager administrator account.</p>\n" +
                  "<p>For more information, refer to the SUSE Manager Installation and Troubleshooting guide or see <b>http://www.suse.com/documentation/suse_manager/</b>.</p>"
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
