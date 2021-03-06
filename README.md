Android-O2OProject
==================

WeChat Public Account Development

这个代码仓库是我的减肥餐O2O项目Android版本开发的代码和apk文件，开发环境是eclipse+ADT+sqlserver
详细设计文档为公司保密信息，暂未分享
主项目是gek文件夹，后台服务端是grill文件夹，其余文件夹均作为lib添加

代码包括了Android端和后台业务逻辑的JAVA代码.
后端数据接口使用了Spring+Mybatis的框架，与数据库进行交互，生成Android端方便调用的JSON
 
Android端主要有网上订餐、查看与管理订单等线上流程，以及菜品展示、客户减肥曲线绘制等功能，

UI与流程示例：
`首页`:
界面完善ing，包括已订购的菜品图片展示，用户的体重曲线记录
![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_1.png)

体重动态输入，并实时显示在曲线上

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_2.png)

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_3.png)

`下单`
点击订购菜单，listview展示套餐列表

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_4.png)

点击金额按钮，进入订单确认页

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_5.png)

输入顾客信息，提交订单

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_6.png)


显示顾客订购的订单列表，并把订单中得菜品展示到首页图片上

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_7.png)

点击餐厅按钮，显示现有餐厅，目前我们只有一家餐厅

![qq 20141231145232](http://images.cnblogs.com/cnblogs_com/melonrice/646682/o_8.png)


项目所使用到的开源框架主要有：

1.fancyCoverFlow进行图片展示
--
用于展示首页的菜品图片列表。
但在读取图片数据时，加载sdcard上的大图片到内存导致了OOM异常
解决办法是基于BitmapFactory.Options类提供的方法定义指定的解码方式在自定义的bitmpa工具类中对图片进行缩放处理，代码参考如下：
```java
  Options opts = new Options();
  opts.inJustDecodeBounds = true;         
  BitmapFactory.decodeFile(filePath, opts);
          
  int imageWidth = opts.outWidth;
  int imageHeight = opts.outHeight;
          
  // 获取屏幕的宽和高        
  Display display = this.getWindowManager().getDefaultDisplay(); // 获取默认窗体显示的对象
  int screenWidth = display.getWidth();
  int screenHeight = display.getHeight();
         
  // 计算缩放比例
  int widthScale = imageWidth / screenWidth;
  int heightScale = imageHeight / screenHeight;
         
  int scale = widthScale > heightScale ? widthScale:heightScale;20         
  // 指定加载可以加载出图片.
  opts.inJustDecodeBounds = false;
  // 使用计算出来的比例进行缩放
  opts.inSampleSize = scale;
  Bitmap bm = BitmapFactory.decodeFile(path, opts);


```


2.hellocharts进行图表绘制
--
hellocharts是github上一个开源的Android原生API写的图表框架，轻量，美观
地址在：https://github.com/lecho/hellocharts-android
在项目中主要用于顾客体重变化曲线图的动态绘制


3.网络请求框架AsyncHttpResponseHandler进行异步加载
--
在Android端的RestClient.java文件中，通过在应用中调用RestClient中的静态方法并重写匿名内部类来加载数据


4.PullToRefresh进行下拉刷新
--
在package com.gez.cookery.jiaoshou.widget; 包下
采用了团队自定义开发的PullToRefreshListView.java
下拉刷新，上拉加载

5.SlidingMenu滑动菜单
--
代码地址：https://github.com/jfeinstein10/SlidingMenu


