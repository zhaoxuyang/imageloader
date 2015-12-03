package com.baidu.iknow.imageloader.request;

import android.support.annotation.Nullable;


public interface DataFetcher<T> {

  interface DataCallback<T> {
    void onDataReady(@Nullable T data);
    void onLoadFailed(Exception e);
  }

  
  void loadData(DataCallback<? super T> callback);


  void cleanup();

  
  void cancel();

  Class<T> getDataClass();

}
