package fybug.nulll.pdcache.supplier;
import java.util.function.Supplier;

import fybug.nulll.pdconcurrent.SyLock;

/**
 * <h2>锁生成接口.</h2>
 * <p>
 * 通过构造时传入锁的类，并自动生成锁的实例。<br/>
 * 发生错误会返回默认的 {@link SyLock#newObjLock()}
 *
 * @author fybug
 * @version 0.0.1
 * @since supplier 0.0.1
 */
final
class SyLockSupp implements Supplier<SyLock> {
    // 锁的类型
    private final Class<? extends SyLock> LOCK_CLASS;

    /** 指定锁的类 */
    SyLockSupp(Class<? extends SyLock> sylock) { LOCK_CLASS = sylock; }

    @Override
    public
    SyLock get() {
        try {
            return LOCK_CLASS.getConstructor().newInstance();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return SyLock.newObjLock();
    }
}
