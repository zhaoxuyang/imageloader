# Image Loader


Android图片加载框架

![Screenshot](https://github.com/zhaoxuyang/imageloader/raw/master/imageloader1.png)


![Screenshot](https://github.com/zhaoxuyang/imageloader/raw/master/imageloader2.png)


## 特性
 * 重写的imageview，使图片数据与控件解耦，控件不会持有图片数据
 * 增加绘制器的概念，达到在不增加内存的情况下支持圆形，圆角矩形以及自定义形状的展示。
 * 支持gif，gif图的所有帧数据保存在native内存，节省java堆内存。支持samplesize，能够显示比较大的gif图
 * 懒加载机制，listview快速滑动的时候不会发起图片加载任务
 * 每个图片加载任务的生命周期由控件决定，控件不可见时则停止。加载任务在失败的时候有重试机制


## 使用方法  

参见TestApplication和TestActivity。


## 参考的其他库

 * [Fresco](https://github.com/facebook/fresco)
 * [Glide](https://github.com/bumptech/glide)
 * [Picasso](https://github.com/square/picasso)


