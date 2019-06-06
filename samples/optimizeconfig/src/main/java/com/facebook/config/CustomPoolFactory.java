package com.facebook.config;

import android.os.Build;
import android.util.SparseIntArray;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.memory.BitmapPool;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.memory.PoolParams;

public class CustomPoolFactory {

  public final static PoolFactory getPoolFactory() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
      PoolParams poolParams = DefaultBitmapPoolParams.get();
      PoolConfig.Builder builder = PoolConfig.newBuilder();
      builder.setBitmapPoolParams(poolParams);
      return new PoolFactory(builder.build());
    } else {
      return new PoolFactory(PoolConfig.newBuilder().build());
    }
  }

  /**
   * Provides pool parameters for {@link BitmapPool}
   */
  private static class DefaultBitmapPoolParams {

    /**
     * We are not reusing Bitmaps and want to free them as soon as possible.
     */
    private static final int MAX_SIZE_SOFT_CAP = 0;
    /**
     * This will cause all get/release calls to behave like alloc/free calls i.e. no pooling.
     */
    private static final SparseIntArray DEFAULT_BUCKETS = new SparseIntArray(0);

    private DefaultBitmapPoolParams() {
    }

    /**
     * Our Bitmaps live in ashmem, meaning that they are pinned in androids' shared native
     * memory. Therefore, we are not constrained by the max heap size of the dalvik heap, but we
     * want to make sure we don't use too much memory on low end devices, so that we don't force
     * other background process to be evicted.
     */
    private static int getMaxSizeHardCap() {
      int maxMemory = (int) Math.min(Runtime.getRuntime().maxMemory(), Integer.MAX_VALUE);
      maxMemory = Math.min(maxMemory / 4, 64 * ByteConstants.MB);
      return maxMemory;
    }

    public static PoolParams get() {
      return new PoolParams(
          MAX_SIZE_SOFT_CAP,
          getMaxSizeHardCap(),
          DEFAULT_BUCKETS,
          0,
          2 * ByteConstants.MB,
//                    Integer.MAX_VALUE,
          PoolParams.IGNORE_THREADS
      );
    }
  }
}
