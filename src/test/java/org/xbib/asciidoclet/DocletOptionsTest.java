package org.xbib.asciidoclet;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.xbib.asciidoclet.DocletOptions.BASEDIR;
import static org.xbib.asciidoclet.DocletOptions.DESTDIR;
import static org.xbib.asciidoclet.DocletOptions.ENCODING;
import static org.xbib.asciidoclet.DocletOptions.OVERVIEW;
import static org.xbib.asciidoclet.DocletOptions.REQUIRE;
import static org.xbib.asciidoclet.DocletOptions.REQUIRE_LONG;
import static org.xbib.asciidoclet.DocletOptions.STYLESHEET;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DocletOptionsTest {

    @Test
    public void testGetBaseDir() {
        assertFalse(DocletOptions.NONE.baseDir().isPresent());
        assertEquals("test", new DocletOptions(new String[][]{{BASEDIR, "test"}}).baseDir().get().getName());
    }

    @Test
    public void testEncoding() {
        assertEquals(Charset.defaultCharset(), DocletOptions.NONE.encoding());
        assertEquals(Charsets.UTF_8, new DocletOptions(new String[][]{{ENCODING, "UTF-8"}}).encoding());
        assertEquals(Charsets.US_ASCII, new DocletOptions(new String[][]{{ENCODING, "US-ASCII"}}).encoding());
        assertEquals(Charsets.ISO_8859_1, new DocletOptions(new String[][]{{ENCODING, "ISO-8859-1"}}).encoding());
    }

    @Test
    public void testOverview() {
        assertFalse(DocletOptions.NONE.overview().isPresent());
        assertEquals("test.adoc", new DocletOptions(new String[][]{{OVERVIEW, "test.adoc"}}).overview().get().getName());
    }

    @Test
    public void testStylesheetFile() {
        assertFalse(DocletOptions.NONE.stylesheet().isPresent());
        assertEquals("foo.css", new DocletOptions(new String[][]{{STYLESHEET, "foo.css"}}).stylesheet().get().getName());
    }

    @Test
    public void testDestDir() {
        assertFalse(DocletOptions.NONE.destDir().isPresent());
        assertEquals("target", new DocletOptions(new String[][]{{DESTDIR, "target"}}).destDir().get().getName());
    }

    @Test
    public void testRequires() {
        assertTrue(DocletOptions.NONE.requires().isEmpty());
        assertThat(new DocletOptions(new String[][]{{REQUIRE, "foo"}, {REQUIRE, "bar"}}).requires(), contains("foo", "bar"));
        assertThat(new DocletOptions(new String[][]{
                {REQUIRE, "a , diagrams/awesome"},
                {REQUIRE_LONG, "bar"},
                {REQUIRE_LONG, "baz,noddy"}}).requires(),
                contains("a", "diagrams/awesome", "bar", "baz", "noddy"));
    }
}
