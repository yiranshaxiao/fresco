package com.facebook.config;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.disk.NoOpDiskTrimmableRegistry;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.listener.BaseRequestListener;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.producers.NetworkFetchProducer;
import com.facebook.imagepipeline.request.ImageRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import okhttp3.OkHttpClient;

public class ImagePipelineConfigUtils {

  public static final String IMAGE_PIPELINE_CACHE_DIR = "image_cache";
  //分配的可用内存
  private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();
  //使用的缓存数量
  private static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 8;
  //默认图极低磁盘空间缓存的最大值
  private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 20 * ByteConstants.MB;
  //默认图低磁盘空间缓存的最大值
  private static final int MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB;
  //默认图磁盘缓存的最大值
  private static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;

  private static MyMemoryTrimmableRegistry sMemoryTrimmableRegistry;

  public static ImagePipelineConfig getDefaultImagePipelineConfig(final Context context) {

    //默认图片的磁盘配置
    DiskCacheConfig diskCacheConfig = DiskCacheConfig
        .newBuilder(context)
        .setBaseDirectoryPath(Environment.getExternalStorageDirectory().getAbsoluteFile())//缓存图片基路径
        .setBaseDirectoryPath(context.getExternalCacheDir()) //文件目录
        .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//文件夹名
        .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
        .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
        .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
        .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
        .build();

    final Context appContext = context.getApplicationContext();
    RequestListener requestListener = new BaseRequestListener() {
      @Override
      public void onRequestFailure(
          ImageRequest request,
          String requestId,
          Throwable throwable,
          boolean isPrefetch) {
        if (throwable instanceof IOException) {
          try {
            //TODO 图片加载错误上报
          } catch (Throwable e) {
          }
        }
      }

      @Override
      public void onProducerFinishWithFailure(
          String requestId,
          String producerName,
          Throwable t,
          Map<String, String> extraMap) {
        if (NetworkFetchProducer.PRODUCER_NAME.equals(producerName)) {
          //TODO 网络图片加载错误上报
          if (t instanceof IOException) {
          }
        }
      }
    };

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        //dns 配置
        .dns(new OkHttpDns())
        //SSL 配置，忽略证书
        .sslSocketFactory(
            HttpsConnectionUtil.getSslSocketFactory(),
            HttpsConnectionUtil.getX509TrustManager())
        .hostnameVerifier(HttpsConnectionUtil.getHostnameVerifier())
        .build();

    Set<RequestListener> requestListeners = new HashSet<RequestListener>(1);
    requestListeners.add(requestListener);
    ActivityManager activityManager =
        (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
    //缓存图片配置
    ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context)
        .setBitmapsConfig(Bitmap.Config.ARGB_8888)
        //OKhttp
        .setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient))
        //关键1
        .setBitmapMemoryCacheParamsSupplier(new CustomBitmapMemoryCacheParamsSupplier(
            activityManager))
        //关键2
        .setEncodedMemoryCacheParamsSupplier(new CustomBitmapMemoryCacheParamsSupplier(
            activityManager))
        //关键3
        .setPoolFactory(CustomPoolFactory.getPoolFactory())
        .setMainDiskCacheConfig(diskCacheConfig)
        .setRequestListeners(requestListeners)
        .setResizeAndRotateEnabledForNetwork(true);
    // http://frescolib.org/docs/resizing-rotating.html#resizing bug on Android 4.4 (KitKat)
    if (Build.VERSION.SDK_INT != 19) {
      configBuilder.setDownsampleEnabled(true);
    }

    sMemoryTrimmableRegistry = new MyMemoryTrimmableRegistry();
    sMemoryTrimmableRegistry.registerMemoryTrimmable(new MemoryTrimmable() {
      @Override
      public void trim(MemoryTrimType trimType) {
        final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();
        if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
            || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio()
            == suggestedTrimRatio
            || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio()
            == suggestedTrimRatio
        ) {
          ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
        }
      }
    });
    configBuilder.setMemoryTrimmableRegistry(sMemoryTrimmableRegistry);

    return configBuilder.build();
  }

  /**
   * {@link android.content.ComponentCallbacks2}
   */
  public static void onTrimMemory(int level) {
    if (sMemoryTrimmableRegistry != null) {
      sMemoryTrimmableRegistry.trim(level);
    }
  }

  public static class MyMemoryTrimmableRegistry implements MemoryTrimmableRegistry {

    private final Set<MemoryTrimmable> sTrimmables =
        Collections.newSetFromMap(new WeakHashMap<MemoryTrimmable, Boolean>());

    @Override
    public void registerMemoryTrimmable(MemoryTrimmable trimmable) {
      sTrimmables.add(trimmable);
    }

    @Override
    public void unregisterMemoryTrimmable(MemoryTrimmable trimmable) {
      sTrimmables.remove(trimmable);
    }

    /**
     * {@link android.content.ComponentCallbacks2}
     */
    void trim(int level) {
      for (MemoryTrimmable trimmable : sTrimmables) {
        if (trimmable != null) {
          trimmable.trim(MemoryTrimType.OnCloseToDalvikHeapLimit);
        }
      }
    }
  }
}