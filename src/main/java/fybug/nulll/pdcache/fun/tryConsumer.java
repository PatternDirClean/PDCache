package fybug.nulll.pdcache.fun;
import java.util.function.Consumer;

/**
 * @author fybug
 * @version 0.0.1
 * @see Consumer
 * @since fun 0.0.1
 */
@FunctionalInterface
public
interface tryConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}
