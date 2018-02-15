package org.xbib.asciidoclet;

import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.asciidoctor.Asciidoctor.Factory.create;

/**
 * Doclet renderer using and configuring Asciidoctor.
 */
public class AsciidoctorRenderer implements DocletRenderer {

    private static final Logger logger = Logger.getLogger(AsciidoctorRenderer.class.getName());

    private static AttributesBuilder defaultAttributes() {
        return AttributesBuilder.attributes()
                .attribute("at", "&#64;")
                .attribute("slash", "/")
                .attribute("icons", null)
                .attribute("idprefix", "")
                .attribute("idseparator", "-")
                .attribute("javadoc", "")
                .attribute("showtitle", true)
                .attribute("source-highlighter", "coderay")
                .attribute("coderay-css", "class")
                .attribute("env-asciidoclet")
                .attribute("env", "asciidoclet");
    }

    private static OptionsBuilder defaultOptions() {
        return OptionsBuilder.options()
                .safe(SafeMode.SAFE)
                .backend("html5");
    }

    protected static final String INLINE_DOCTYPE = "inline";

    private final Asciidoctor asciidoctor;
    private final Optional<OutputTemplates> templates;
    private final Options options;

    public AsciidoctorRenderer(DocletOptions docletOptions, DocErrorReporter errorReporter) {
        this(docletOptions, errorReporter, OutputTemplates.create(errorReporter), create(docletOptions.gemPath()));
    }

    /**
     * Constructor used directly for testing purposes only.
     * @param docletOptions doclet options
     * @param errorReporter error reporter
     * @param templates templates
     * @param asciidoctor Asciidoctor
     */
    protected AsciidoctorRenderer(DocletOptions docletOptions, DocErrorReporter errorReporter, Optional<OutputTemplates> templates, Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.templates = templates;
        this.options = buildOptions(docletOptions, errorReporter);
    }

    private Options buildOptions(DocletOptions docletOptions, DocErrorReporter errorReporter) {
        OptionsBuilder opts = defaultOptions();
        if (docletOptions.baseDir().isPresent()) {
            opts.baseDir(docletOptions.baseDir().get());
        }
        templates.ifPresent(outputTemplates -> opts.templateDir(outputTemplates.templateDir()));
        opts.attributes(buildAttributes(docletOptions, errorReporter));
        if (docletOptions.requires().size() > 0) {
            for (String require : docletOptions.requires()) {
                asciidoctor.rubyExtensionRegistry().requireLibrary(require);
            }
        }
        return opts.get();
    }

    private Attributes buildAttributes(DocletOptions docletOptions, DocErrorReporter errorReporter) {
        return defaultAttributes()
                .attributes(new AttributesLoader(asciidoctor, docletOptions, errorReporter).load())
                .get();
    }

    /**
     * Renders a generic document (class, field, method, etc).
     *
     * @param doc input
     */
    @Override
    public void renderDoc(Doc doc) {
        // hide text that looks like tags (such as annotations in source code) from Javadoc
        doc.setRawCommentText(doc.getRawCommentText().replaceAll("@([A-Z])", "{@literal @}$1"));
        StringBuilder buffer = new StringBuilder();
        try {
            buffer.append(render(doc.commentText(), false));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "doc: " + doc.name() + ": failed to render doc comment: " + doc.commentText() + " reason: " + e.getMessage(), e);
        }
        buffer.append('\n');
        for (Tag tag : doc.tags()) {
            try {
                renderTag(tag, buffer);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "doc: " + doc.name() + ": failed to render tag: " + tag + " reason: " + e.getMessage(), e);
            }
            buffer.append('\n');
        }
        doc.setRawCommentText(buffer.toString());
    }

    public void cleanup() {
        templates.ifPresent(OutputTemplates::delete);
    }

    /**
     * Renders a document tag in the standard way.
     *
     * @param tag input
     * @param buffer output buffer
     */
    private void renderTag(Tag tag, StringBuilder buffer) throws Exception {
        buffer.append(tag.name()).append(' ');
        // Special handling for @param <T> tags
        // See http://docs.oracle.com/javase/1.5.0/docs/tooldocs/windows/javadoc.html#@param
        if ((tag instanceof ParamTag) && ((ParamTag) tag).isTypeParameter()) {
            ParamTag paramTag = (ParamTag) tag;
            buffer.append("<").append(paramTag.parameterName()).append(">");
            String text = paramTag.parameterComment();
            if (text.length() > 0) {
                buffer.append(' ').append(render(text, true));
            }
            return;
        }
        buffer.append(render(tag.text(), true));
    }

    /**
     * Renders the input using Asciidoctor.
     *
     * The source is first cleaned by stripping any trailing space after an
     * end line (e.g., `"\n "`), which gets left behind by the Javadoc
     * processor.
     *
     * @param input AsciiDoc source
     * @return content rendered by Asciidoctor
     */
    private String render(String input, boolean inline) throws Exception {
        if (input.trim().isEmpty()) {
            return "";
        }
        options.setDocType(inline ? INLINE_DOCTYPE : null);
        String clean = cleanJavadocInput(input);
        return clean.isEmpty() ? "" : asciidoctor.render(clean, options);
    }

    protected static String cleanJavadocInput(String input) {
        return input.trim()
            .replaceAll("\n ", "\n") // Newline space to accommodate javadoc newlines.
            .replaceAll("\\{at}", "&#64;") // {at} is translated into @.
            .replaceAll("\\{slash}", "/") // {slash} is translated into /.
            .replaceAll("(?m)^( *)\\*\\\\/$", "$1*/") // Multi-line comment end tag is translated into */.
            .replaceAll("\\{@literal (.*?)}", "$1"); // {@literal _} is translated into _ (standard javadoc).
    }
}
