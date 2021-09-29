package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;

import lombok.experimental.UtilityClass;

/**
 * emmmm....就个工具类
 *
 * @author fybug
 * @version 0.0.1
 * @since PDCache 0.0.1
 */
@UtilityClass
public
class CacheGcThrea {
    /**
     * 为指定对象绑定回收接口
     *
     * @see Cleaner#register(Object, Runnable)
     */
    @NotNull
    public
    Cleaner.Cleanable binClean(@NotNull Object obj, @NotNull Runnable run)
    { return Cleaner.create().register(obj, run); }
}