package fybug.nulll.pdcache.fun;
import java.util.function.Function;

/**
 * @author fybug
 * @version 0.0.1
 * @see Function
 * @since fun 0.0.1
 */
@FunctionalInterface
public
interface tryFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
