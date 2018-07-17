package com.facebook.samples.comparison.viewhelper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.samples.comparison.adapters.FrescoAdapterV2;
import com.facebook.samples.comparison.configs.imagepipeline.IdlePipelineDraweeController;
import com.facebook.samples.comparison.instrumentation.InstrumentedDraweeViewV2;

/**
 * Created on 17/07/2018.
 */
public class RecyclerViewHelper extends RecyclerView.OnScrollListener {

  private RecyclerView mRecyclerView;

  public void with(RecyclerView recyclerView) {
    this.mRecyclerView = recyclerView;
    this.mRecyclerView.addOnScrollListener(this);
  }

  @Override
  public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    super.onScrollStateChanged(recyclerView, newState);
    if (recyclerView.getAdapter() instanceof FrescoAdapterV2) {
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        Fresco.getImagePipeline().resume();
        IdlePipelineDraweeController.setIdle(true);
        InstrumentedDraweeViewV2.setIdleState(true);
        onIdle();
      } else {
        Fresco.getImagePipeline().pause();
        IdlePipelineDraweeController.setIdle(false);
        InstrumentedDraweeViewV2.setIdleState(false);
      }
    }
  }

  private void onIdle() {
    if (mRecyclerView == null) {
      return;
    }
    if (mRecyclerView.getLayoutManager() == null) {
      return;
    }
    if (mRecyclerView.getAdapter() == null) {
      return;
    }
    RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
    for (int i = 0, count = manager.getChildCount(); i < count; i++) {
      View view = manager.getChildAt(i);
      if (view != null) {
        RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
        if (viewHolder instanceof IdleListener) {
          ((IdleListener) viewHolder).onIdle();
        }
      }
    }
  }

  @Override
  public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);
  }

  public interface IdleListener {

    void onIdle();
  }
}
