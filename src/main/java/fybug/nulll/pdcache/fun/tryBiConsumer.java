package fybug.nulll.pdcache.fun;
import java.util.function.BiConsumer;

/**
 * @author fybug
 * @version 0.0.1
 * @see BiConsumer
 * @since fun 0.0.1
 */
@FunctionalInterface
public
interface tryBiConsumer<T, U, E extends Exception> {
    void accept(T t, U u) throws E;
}
