package com.facebook.config;

import android.app.ActivityManager;
import android.os.Build;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import java.util.concurrent.TimeUnit;

public class CustomBitmapMemoryCacheParamsSupplier implements Supplier<MemoryCacheParams> {

  private ActivityManager activityManager;

  public CustomBitmapMemoryCacheParamsSupplier(ActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @Override
  public MemoryCacheParams get() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
      int maxCacheSize = Math.min(getMaxCacheSize(), 16 * ByteConstants.MB);
      int maxCacheEntrySize = 2 * ByteConstants.MB;
      int entries = maxCacheSize / maxCacheEntrySize * 2;
      return new MemoryCacheParams(
          maxCacheSize,
          entries,
          maxCacheSize,
          entries,
          maxCacheEntrySize,
          TimeUnit.MINUTES.toMillis(5));
    } else {
      return new MemoryCacheParams(
          getMaxCacheSize(),
          256,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          TimeUnit.MINUTES.toMillis(5));
    }
  }

  private int getMaxCacheSize() {
    final int maxMemory =
        Math.min(activityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);

    if (maxMemory < 32 * ByteConstants.MB) {
      return 4 * ByteConstants.MB;
    } else if (maxMemory < 64 * ByteConstants.MB) {
      return 6 * ByteConstants.MB;
    } else {
      // We don't want to use more ashmem on Gingerbread for now, since it doesn't respond well to
      // native memory pressure (doesn't throw exceptions, crashes app, crashes phone)
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
        return 8 * ByteConstants.MB;
      } else {
        return maxMemory / 8;
      }
    }
  }
}
