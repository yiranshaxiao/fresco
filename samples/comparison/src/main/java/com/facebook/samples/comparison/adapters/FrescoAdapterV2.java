/*
 * This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.samples.comparison.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.ImmutableList;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerFactory;
import com.facebook.drawee.components.DeferredReleaser;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.drawable.DrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.samples.comparison.Drawables;
import com.facebook.samples.comparison.configs.imagepipeline.IdlePipelineDraweeController;
import com.facebook.samples.comparison.holders.FrescoHolderV2;
import com.facebook.samples.comparison.instrumentation.InstrumentedDraweeViewV2;
import com.facebook.samples.comparison.instrumentation.PerfListener;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/**
 * RecyclerView Adapter for Fresco
 */
public class FrescoAdapterV2 extends ImageListAdapter {

  public FrescoAdapterV2(
      Context context,
      PerfListener perfListener,
      ImagePipelineConfig imagePipelineConfig) {
    super(context, perfListener);
    Fresco.initialize(
        context,
        imagePipelineConfig,
        DraweeConfig.newBuilder()
            .setPipelineDraweeControllerFactory(new PipelineDraweeControllerFactory() {
              @Override
              protected PipelineDraweeController internalCreateController(
                  Resources resources,
                  DeferredReleaser deferredReleaser,
                  DrawableFactory animatedDrawableFactory,
                  Executor uiThreadExecutor,
                  MemoryCache<CacheKey, CloseableImage> memoryCache,
                  @Nullable ImmutableList<DrawableFactory> drawableFactories) {
                return new IdlePipelineDraweeController(
                    resources,
                    deferredReleaser,
                    animatedDrawableFactory,
                    uiThreadExecutor,
                    memoryCache,
                    drawableFactories);
              }
            }).build());
  }

  @Override
  public FrescoHolderV2 onCreateViewHolder(ViewGroup parent, int viewType) {
    GenericDraweeHierarchy gdh = new GenericDraweeHierarchyBuilder(getContext().getResources())
        .setPlaceholderImage(Drawables.sPlaceholderDrawable)
        .setFailureImage(Drawables.sErrorDrawable)
        .setProgressBarImage(new ProgressBarDrawable())
        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
        .build();
    final InstrumentedDraweeViewV2 instrView = new InstrumentedDraweeViewV2(getContext(), gdh);

    return new FrescoHolderV2(getContext(), parent, instrView, getPerfListener());
  }

  @Override
  public void shutDown() {
    Fresco.shutDown();
  }
}
