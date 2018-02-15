package org.xbib.asciidoclet;

import com.sun.javadoc.DocErrorReporter;
import org.junit.Before;
import org.junit.Test;
import org.xbib.asciidoclet.DocletOptions;
import org.xbib.asciidoclet.Stylesheets;

import static org.xbib.asciidoclet.Stylesheets.JAVA6_STYLESHEET;
import static org.xbib.asciidoclet.Stylesheets.JAVA8_STYLESHEET;
import static org.xbib.asciidoclet.Stylesheets.JAVA9_STYLESHEET;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class StylesheetsTest {

    private Stylesheets stylesheets;
    private DocErrorReporter mockErrorReporter;

    @Before
    public void setup() {
        mockErrorReporter = mock(DocErrorReporter.class);
        stylesheets = new Stylesheets(DocletOptions.NONE, mockErrorReporter);
    }

    @Test
    public void java9ShouldSelectStylesheet9() {
        assertEquals(JAVA9_STYLESHEET, stylesheets.selectStylesheet("9.0.4.1+11"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java8ShouldSelectStylesheet8() {
        assertEquals(JAVA8_STYLESHEET, stylesheets.selectStylesheet("1.8.0_11"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java7ShouldSelectStylesheet8() {
        assertEquals(JAVA8_STYLESHEET, stylesheets.selectStylesheet("1.7.0_51"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java6ShouldSelectStylesheet6() {
        assertEquals(JAVA6_STYLESHEET, stylesheets.selectStylesheet("1.6.0_45"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void java5ShouldSelectStylesheet6() {
        assertEquals(JAVA6_STYLESHEET, stylesheets.selectStylesheet("1.5.0_22"));
        verifyNoMoreInteractions(mockErrorReporter);
    }

    @Test
    public void unknownJavaShouldSelectStylesheet9AndWarn()  {
        assertEquals(JAVA9_STYLESHEET, stylesheets.selectStylesheet("47.11"));
        verify(mockErrorReporter).printWarning(anyString());
    }
}
