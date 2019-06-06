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
package com.facebook.samples.optimizeconfig;

import android.app.Application;
import android.content.Context;
import com.facebook.common.logging.FLog;
import com.facebook.config.ImagePipelineConfigUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

/**
 * Optimize Config Application implementation where we set up Fresco
 */
public class OptimizeConfigApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    init(this);
  }

  private void init(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("context cannot be null.");
    }
    Context appContext = context.getApplicationContext();
    ImagePipelineConfig config = ImagePipelineConfigUtils.getDefaultImagePipelineConfig(appContext);
    Fresco.initialize(appContext, config);
    if (BuildConfig.DEBUG) {
      FLog.setMinimumLoggingLevel(FLog.INFO);
    } else {
      FLog.setMinimumLoggingLevel(FLog.ERROR);
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    try {
      Fresco.getImagePipeline().clearMemoryCaches();
    } catch (Throwable t) {
    }
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    try {
      ImagePipelineConfigUtils.onTrimMemory(level);
    } catch (Throwable t) {

    }
  }
}
