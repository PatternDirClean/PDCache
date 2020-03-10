package fybug.nulll.pdcache.fun;

/**
 * @author fybug
 * @version 0.0.1
 * @see Runnable
 * @since fun 0.0.2
 */
@FunctionalInterface
public
interface tryRunnable<E extends Exception> {
    void run() throws E;
}
