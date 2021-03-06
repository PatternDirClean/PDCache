package fybug.nulll.pdcache.supplier;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdcache.CanClean;

import static fybug.nulll.pdcache.RunTest.to;

@RunWith( Suite.class )
@Suite.SuiteClasses( {memoryTest.class} )
public
class suppilerTest {
    private static CanClean canClean = null;

    public static
    CanClean getNowClean() { return canClean; }

    public static
    CanClean nextClean() { return canClean = new a(); }

    public static
    void destruction() {
        canClean = null;
        System.gc();
    }

    private static
    class a implements CanClean {

        public @NotNull
        Runnable getclean() {
            var s = this.toString();
            return () -> to.println("des:" + s);
        }
    }
}
