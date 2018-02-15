package org.xbib.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

import java.util.Locale;
import java.util.Set;

public class Asciidoclet extends Doclet {

    private final RootDoc rootDoc;
    private final DocletOptions docletOptions;
    private final DocletIterator iterator;
    private final Stylesheets stylesheets;

    public Asciidoclet(RootDoc rootDoc) {
        this.rootDoc = rootDoc;
        this.docletOptions = new DocletOptions(rootDoc);
        this.iterator = new DocletIterator(docletOptions);
        this.stylesheets = new Stylesheets(docletOptions, rootDoc);
    }

    // test use
    Asciidoclet(RootDoc rootDoc, DocletIterator iterator, Stylesheets stylesheets) {
        this.rootDoc = rootDoc;
        this.docletOptions = new DocletOptions(rootDoc);
        this.iterator = iterator;
        this.stylesheets = stylesheets;
    }

    /**
     * Sets the language version to Java 5.
     *
     * _Javadoc spec requirement._
     *
     * @return language version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
    /**
     * Sets the option length to the standard Javadoc option length.
     *
     * _Javadoc spec requirement._
     *
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        return optionLength(option, new StandardAdapter());
    }

    /**
     * The starting point of Javadoc render.
     *
     * _Javadoc spec requirement._
     *
     * @param rootDoc the root doc
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        return new Asciidoclet(rootDoc).start(new StandardAdapter());
    }

    /**
     * Processes the input options by delegating to the standard handler.
     *
     * _Javadoc spec requirement._
     *
     * @param options input option array
     * @param errorReporter error handling
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return validOptions(options, errorReporter, new StandardAdapter());
    }

    static int optionLength(String option, StandardAdapter standardDoclet) {
        return DocletOptions.optionLength(option, standardDoclet);
    }

    static boolean validOptions(String[][] options, DocErrorReporter errorReporter, StandardAdapter standardDoclet) {
        return DocletOptions.validOptions(options, errorReporter, standardDoclet);
    }

    boolean start(StandardAdapter standardDoclet) {
        return run(standardDoclet)
                && postProcess();
    }

    private boolean run(StandardAdapter standardDoclet) {
        AsciidoctorRenderer renderer = new AsciidoctorRenderer(docletOptions, rootDoc);
        try {
            return iterator.render(rootDoc, renderer) &&
                    standardDoclet.start(rootDoc);
        } finally {
            renderer.cleanup();
        }
    }

    private boolean postProcess() {
        return docletOptions.stylesheet().isPresent() || stylesheets.copy();
    }
}
