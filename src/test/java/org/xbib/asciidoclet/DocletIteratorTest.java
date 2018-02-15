package org.xbib.asciidoclet;

import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocletIteratorTest {

    private DocletRenderer mockRenderer;
    private RootDoc mockDoc;
    private ClassDoc mockClassDoc;
    private PackageDoc mockPackageDoc;
    private FieldDoc mockFieldDoc;
    private FieldDoc mockEnumFieldDoc;
    private ConstructorDoc mockConstructorDoc;
    private MethodDoc mockMethodDoc;

    @Before
    public void setup() {
        mockRenderer = mock(DocletRenderer.class);

        mockDoc = mock(RootDoc.class);
        mockPackageDoc = mock(PackageDoc.class);
        mockFieldDoc = mock(FieldDoc.class);
        mockEnumFieldDoc = mock(FieldDoc.class);
        mockConstructorDoc = mock(ConstructorDoc.class);
        mockMethodDoc = mock(MethodDoc.class);
        mockClassDoc = mockClassDoc(ClassDoc.class, mockPackageDoc, mockFieldDoc, mockEnumFieldDoc, mockConstructorDoc, mockMethodDoc);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockDoc.options()).thenReturn(new String[][]{});
    }

    private <T extends ClassDoc> T mockClassDoc(Class<T> type, PackageDoc packageDoc, FieldDoc fieldDoc, FieldDoc enumConstants, ConstructorDoc constructorDoc, MethodDoc methodDoc) {
        T classDoc = mock(type);
        when(classDoc.containingPackage()).thenReturn(packageDoc);
        when(classDoc.fields()).thenReturn(new FieldDoc[]{fieldDoc});
        when(classDoc.constructors()).thenReturn(new ConstructorDoc[]{constructorDoc});
        when(classDoc.methods()).thenReturn(new MethodDoc[]{methodDoc});
        when(classDoc.enumConstants()).thenReturn(new FieldDoc[]{enumConstants});
        return classDoc;
    }

    @Test
    public void testIteration() {
        new DocletIterator(DocletOptions.NONE).render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockFieldDoc);
        verify(mockRenderer).renderDoc(mockConstructorDoc);
        verify(mockRenderer).renderDoc(mockMethodDoc);
        verify(mockRenderer).renderDoc(mockPackageDoc);
        verify(mockRenderer).renderDoc(mockEnumFieldDoc);
    }

    @Test
    public void testAnnotationIteration() {
        AnnotationTypeDoc mockClassDoc = mockClassDoc(AnnotationTypeDoc.class, mockPackageDoc, mockFieldDoc, mockEnumFieldDoc, mockConstructorDoc, mockMethodDoc);
        AnnotationTypeElementDoc mockAnnotationElement = mock(AnnotationTypeElementDoc.class);

        when(mockDoc.classes()).thenReturn(new ClassDoc[]{mockClassDoc});
        when(mockClassDoc.elements()).thenReturn(new AnnotationTypeElementDoc[]{mockAnnotationElement});

        new DocletIterator(DocletOptions.NONE).render(mockDoc, mockRenderer);

        verify(mockRenderer).renderDoc(mockClassDoc);
        verify(mockRenderer).renderDoc(mockAnnotationElement);
    }

    @Test
    public void testIgnoreNonAsciidocOverview() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "foo.html"}}));

        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockDoc, never()).setRawCommentText(any(String.class));
    }

    @Test
    public void testFailIfAsciidocOverviewNotFound() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "notfound.adoc"}}));

        assertFalse(iterator.render(mockDoc, mockRenderer));
    }

    @Test
    public void testOverviewFound() {
        DocletIterator iterator = new DocletIterator(new DocletOptions(new String[][] {{DocletOptions.OVERVIEW, "src/docs/asciidoclet/overview.adoc"}}));
        assertTrue(iterator.render(mockDoc, mockRenderer));
        verify(mockRenderer).renderDoc(mockDoc);
    }
}
