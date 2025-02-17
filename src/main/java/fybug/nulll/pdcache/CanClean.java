package fybug.nulll.pdcache;
import java.lang.ref.Cleaner.Cleanable;

import jakarta.validation.constraints.NotNull;

/**
 * <h2>拥有清理回调的对象.</h2>
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
  Runnable getclean() { return () -> {}; }
}
