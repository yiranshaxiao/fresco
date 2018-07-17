package com.facebook.samples.comparison.configs.imagepipeline;

import android.content.res.Resources;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.ImmutableList;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.components.DeferredReleaser;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.drawable.DrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/**
 * Created on 17/07/2018.
 */
public class IdlePipelineDraweeController extends PipelineDraweeController {

  private static boolean sIdle = true;

  public IdlePipelineDraweeController(
      Resources resources,
      DeferredReleaser deferredReleaser,
      DrawableFactory animatedDrawableFactory,
      Executor uiThreadExecutor,
      @Nullable MemoryCache<CacheKey, CloseableImage> memoryCache,
      @Nullable ImmutableList<DrawableFactory> globalDrawableFactories) {
    super(
        resources,
        deferredReleaser,
        animatedDrawableFactory,
        uiThreadExecutor,
        memoryCache,
        globalDrawableFactories);
  }

  public static void setIdle(boolean idle) {
    sIdle = idle;
  }

  @Override
  public void onAttach() {
    if (sIdle) {
      super.onAttach();
    }
  }

}
