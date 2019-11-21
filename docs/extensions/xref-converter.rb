###
### This is the AsciiDoctor-PDF xref-converter template for SUSE/Uyuni Documentation
###
### Joseph Cayouette (jcayouette@suse.com), 2019
### Klaus Kaempf (kkaempf@suse.de), 2019
###
class PDFConverter < (Asciidoctor::Converter.for 'pdf')
  register_for 'pdf'
  # asciidoctor-pdf -b pdf -r ./tanchor/inline_anchor.rb test.adoc
  def convert_inline_anchor node
    #puts "Ooh! Look! A Converter for #{node.class.inspect} of type #{node.type.inspect} xref #{node.attributes.inspect}"
    unless node.attr('path')
      # path == nil means internal reference
      return super
    end
    @caret ||= (load_theme node.document).menu_caret_content || %( \u203a )
    # this should be <module>:{<subdir>/}filename
    refid = node.attr('refid')
    unless refid
      return super
    end
    fragment = node.attr('fragment')
    if fragment
      return super
    end
    #puts "path #{node.attr('path').inspect}, refid #{refid.inspect}"
    # break at :, note: can't use 'module' here (reserved word),
    xmodule, path = refid.split(':')
    unless path
      return super
    end
    # break path at /
    subdir, filename = path.split('/')
    title = node.text
    out = [ xmodule.capitalize ]
    out << subdir.capitalize if subdir
    out << title
    #puts "\t#{out.join(@caret)}"
    %(<strong>[ #{out.join(@caret)} ]</strong>)
#    if node.type == :xref
#      @caret ||= (load_theme node.document).menu_caret_content || %( \u203a )
#      puts "node is a #{node.class}"
#      puts "node.text #{node.text.inspect}"
#      puts "node.type #{node.type.inspect}"
#      puts "node.target #{node.target.inspect}"
#      puts "node.attributes #{node.attributes.inspect}"
#      exit 1
#      title = node.text
#      path =  node.attr('refid').split(':').map do |element|
#        element.split("-").map {|word| word.capitalize}.join(" ")
#      end
#      path = path.join(caret)
#      %(<strong>[ #{path} #{caret} ] </strong>)
#    else
#      super
#    end
  end
end

#REMARKS: Dan Allen Comments
# FYI, node.attr 'refid' is what contains the page ID, minus the .adoc file extension
# you can see how I extract information from the xref in Antora here: https://gitlab.com/antora/antora/blob/master/packages/asciidoc-loader/lib/converter/html5.js
# If the path attribute is set, you know it's an interdocument xref (as opposed to an in-page anchor)
# You have to skip any xref that doesn't have the 'path' attribute set
# The presence of the 'path' attribute tells you that it's not an internal reference
#{title}
# TODO drop the caret at the end of a the path, if there is a subfolder we need to drop '/' and add a space.
#
# We style our xrefs in 3 ways:
#
#  Specify [MODULE, FILENAME, TITLE]
# xref:reference:filename.adoc[Title]
#
# Specify [MODULE, SUBDIR, FILENAME, TITLE]
# xref:reference:systems/filename.adoc[Title]
#
# Specify Specific [MODULE, SUBDIR, FILENAME, SECTION-ID, TITLE]
# xref:reference:systems/filename.adoc#SECT-ID[Title]
#
# These should end up looking like one of the following in PDF:
#
# [ Reference > Systems > Filename,  Section: TITLE ]
#
# or:
#
# [ Reference > Systems > Filename ]
#
# without subdir:
#
# [ Administration > Image Building]
#
# or:
#
# [ Administration > Image Building, Section: TITLE]
#
# We need to skip internal references <<cve-maintenance>> without the path attribute set Otherwise all of our internal refs are converted into a
# non clickable link. We need to skip and preserve these.
#
# Example
#
# In .adoc
#   xref:reference:home/home-notification-messages.adoc[Notification Messages]
# should be converted to
#   [ Reference > Home > Notification Messages]
#
# Generic case
#
#  xref:<module>:{<subdir>/}filename.adoc[<title>]
#  (<subdir>/} is optional
#
# [ <Module> > <Subdir> > <title> ]
#
