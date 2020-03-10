package fybug.nulll.pdcache.fun;
import java.util.function.Supplier;

/**
 * @author fybug
 * @version 0.0.1
 * @see Supplier
 * @since fun 0.0.1
 */
@FunctionalInterface
public
interface trySupplier<V, E extends Exception> {
    V get() throws E;
}
