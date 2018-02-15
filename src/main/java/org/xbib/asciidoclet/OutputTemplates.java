package org.xbib.asciidoclet;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.javadoc.DocErrorReporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Sets up a temporary directory containing output templates for use by Asciidoctor.
 */
public class OutputTemplates {

    private static final String[] TEMPLATE_NAMES = new String[] {
            "section.html.haml",
            "paragraph.html.haml"
    };

    private final File templateDir;

    private OutputTemplates(File templateDir) {
        this.templateDir = templateDir;
    }

    static Optional<OutputTemplates> create(DocErrorReporter errorReporter) {
        File dir = prepareTemplateDir(errorReporter);
        return dir == null ? Optional.empty() : Optional.of(new OutputTemplates(dir));
    }

    File templateDir() {
        return templateDir;
    }

    void delete() {
        for (String templateName : TEMPLATE_NAMES) {
            new File(templateDir, templateName).delete();
        }
        templateDir.delete();
    }

    private static File prepareTemplateDir(DocErrorReporter errorReporter) {
        // copy our template resources to the templateDir so Asciidoctor can use them.
        File templateDir = Files.createTempDir();
        try {
            for (String templateName : TEMPLATE_NAMES) {
                prepareTemplate(templateDir, templateName);
            }
            return templateDir;
        } catch (IOException e) {
            errorReporter.printWarning("Failed to prepare templates: " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void prepareTemplate(File templateDir, String template) throws IOException {
        URL src = OutputTemplates.class.getClassLoader().getResource("templates/" + template);
        if (src == null) {
            throw new IOException("Could not find template " + template);
        }
        ByteSink dest = Files.asByteSink(new File(templateDir, template));
        Resources.asByteSource(src).copyTo(dest);
    }

}
