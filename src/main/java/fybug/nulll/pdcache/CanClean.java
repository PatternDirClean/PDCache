package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner.Cleanable;

/**
 * 拥有清理回调的对象
 *
 * @author fybug
 * @version 0.0.1
 * @see Cleanable#register(Object, Runnable)
 * @since PDCache 0.0.1
 */
public
interface CanClean {
    /** 生成一个清理回调，在对象被回收的时候会调用该回调 */
    @NotNull
    default
    Runnable getclean() {return () -> {};}
}
