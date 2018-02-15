package org.xbib.asciidoclet;

import com.sun.javadoc.Doc;

/**
 * Interface used to render a Javadoc Doc.
 */
public interface DocletRenderer {

    void renderDoc(Doc doc);
}
