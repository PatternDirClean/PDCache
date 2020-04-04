package fybug.nulll.pdcache.memory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith( Suite.class )
@Suite.SuiteClasses( {CacheTest.class, MapCacheTest.class} )
public
class memoryTest {}
