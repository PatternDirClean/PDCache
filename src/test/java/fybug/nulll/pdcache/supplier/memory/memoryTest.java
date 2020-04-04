package fybug.nulll.pdcache.supplier.memory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fybug.nulll.pdcache.memory.CacheTest;
import fybug.nulll.pdcache.memory.MapCacheTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {SCacheTest.class, SMapCacheTest.class} )
public
class memoryTest {}
