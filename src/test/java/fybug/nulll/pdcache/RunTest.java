package fybug.nulll.pdcache;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import fybug.nulll.pdcache.memory.memoryTest;
import fybug.nulll.pdcache.supplier.suppilerTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {memoryTest.class, suppilerTest.class} )
public
class RunTest {
    public static PrintWriter from;
    public static PrintWriter to;

    public static StringWriter from_s;
    public static StringWriter to_s;

    public static
    void init() {
        from = new PrintWriter(from_s = new StringWriter());
        to = new PrintWriter(to_s = new StringWriter());
    }

    public static
    void destruction() throws IOException {
        suppilerTest.destruction();

        from.close();
        from = null;
        from_s.close();
        from_s = null;

        to.close();
        to = null;
        to_s.close();
        to_s = null;
    }

    public static
    void check() { Assert.assertEquals(from_s.toString(), to_s.toString()); }
}
