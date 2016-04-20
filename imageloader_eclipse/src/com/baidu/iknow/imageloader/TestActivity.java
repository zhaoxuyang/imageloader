package com.baidu.iknow.imageloader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawer.DrawerFactory;
import com.baidu.iknow.imageloader.widgets.CustomImageView;
import com.baidu.iknow.imageloader.widgets.CustomImageView.CustomImageBuilder;
import com.baidu.iknow.imageloader.widgets.CustomImageView.MatrixScaleType;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

public class TestActivity extends Activity {

    private ListView listView;

    private ArrayList<ItemData> mdatas = new ArrayList<TestActivity.ItemData>();

    private ScaleType scaleType = ScaleType.FIT_XY;
    
    private MatrixScaleType matrixScaleType = MatrixScaleType.MATRIX;

    private int drawerType = 0;

    private MyAdapter adapter = new MyAdapter();

    public static HashMap<String, CustomDrawable> drawables = new HashMap<String, CustomDrawable>();

    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // try {

        setContentView(R.layout.activity_list_item);
        addData("http://img.name2012.com/uploads/allimg/2015-06/30-023131_451.jpg");
        addData("http://www.baidu.com");
        addData("http://tb.himg.baidu.com/sys/portrait/item/6ebee68891e69c89e5a5bde5908de5ad9779799774");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=97a22355e6fe9925cb0c695804a95ee4/f0160924ab18972b798c3b5ae0cd7b899f510aef.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=837eee5e9f25bc312b5d01906ede8de7/2924ab18972bd4079b71e86d7d899e510eb309ef.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=4f759785ba315c6043956be7bdb0cbe6/8b18972bd40735fa4946752998510fb30e2408ef.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=29aeb2915a6034a829e2b889fb1249d9/b72bd40735fae6cdcb0290f109b30f2443a70fef.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=bdea2694329b033b2c88fcd225cf3620/b925bc315c6034a8371f8311cd134954082376a4.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=6dbc92b6fbfaaf5184e381b7bc5594ed/2ef41bd5ad6eddc4f27d8e6b3fdbb6fd53663383.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=68fc5de3fdf2b211e42e8546fa816511/f158ccbf6c81800a611cb29eb73533fa838b47a0.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=f1ec4c07df33c895a67e9873e1127397/b18fa0ec08fa513dd85f574e3b6d55fbb3fbd9ad.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=fe854cb66b224f4a5799731b39f69044/013fb80e7bec54e77aaf208ebf389b504ec26ada.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=52f723ec7ef0f736d8fe4c093a54b382/662309f7905298228b9d2c50d1ca7bcb0b46d4f3.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=0ba58f607f310a55c424defc87444387/b822720e0cf3d7ca87582ae5f41fbe096a63a9f3.jpg");
        addData("https://raw.githubusercontent.com/Microsoft/Windows-universal-samples/master/Samples/XamlAnimatedGif/cs/Images/sample.gif");
        addData("http://d.hiphotos.baidu.com/forum/eWH=150,150;bp=1090008,0,0,200/sign=6238473d6a81800a7c8ff403800003d6/d6ca7bcb0a46f21f0d69e0e1f0246b600c33ae27.jpg");
        addData("http://e.hiphotos.baidu.com/album/pic/item/63d0f703918fa0ecc471337c279759ee3d6ddb09.jpg");
        addData("http://imgsrc.baidu.com/forum/pic/item/7aafa40f4bfbfbed25cc452078f0f736aec31fd3.jpg");
        addData("http://img1.imgtn.bdimg.com/it/u=1319768910,3703557557&fm=21&gp=0.jpg");
        addData("http://img5.duitang.com/uploads/item/201411/25/20141125200101_QQjNY.gif");
        addData("http://pic1.win4000.com/wallpaper/7/53ab8186dcf27.jpg");
        addData("http://pic1.win4000.com/wallpaper/7/53ab8186dcf27.jpg");
        addData("http://imgsrc.baidu.com/forum/w%3D580/sign=06cac2a9b999a9013b355b3e2d940a58/c90022d0f703918fb9162377503d269758eec46b.jpg");
        addData("http://pic1.win4000.com/wallpaper/7/53ab81a013503.jpg");
        addData("http://img5.duitang.com/uploads/item/201508/12/20150812204032_eiAQk.thumb.224_0.jpeg");
        addData("http://cdn.duitang.com/uploads/blog/201306/14/20130614102318_YxnEM.gif");
        addData("http://b.hiphotos.baidu.com/zhidao/pic/item/4b90f603738da97728164341b651f8198618e39b.jpg");
        addData("http://img5.duitang.com/uploads/item/201411/13/20141113191932_UTUy8.thumb.224_0.jpeg");
        addData("http://pic1.win4000.com/wallpaper/7/53ab81a013503.jpg");
        mdatas.addAll(mdatas);
        mdatas.addAll(mdatas);
        mdatas.addAll(mdatas);

        addData("http://img4.duitang.com/uploads/item/201502/15/20150215224242_Z5mBC.thumb.224_0.jpeg");
        addData("http://www.ittribalwo.com/upfiles/image/20140506181328.gif");
        addData("http://g.hiphotos.baidu.com/image/pic/item/0df3d7ca7bcb0a46b48645306963f6246b60af17.jpg");
        addData("http://g.hiphotos.baidu.com/image/pic/item/b58f8c5494eef01f84ccf8cce2fe9925bd317ddb.jpg");
        addData("http://e.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=0f3a945ca18b87d65017a31b3238040e/fd039245d688d43f3bb2b79f7d1ed21b0ef43b47.jpg");
        addData("http://b.hiphotos.baidu.com/image/pic/item/18d8bc3eb13533fa3b2655c7abd3fd1f40345b43.jpg");
        addData("http://b.hiphotos.baidu.com/image/pic/item/faf2b2119313b07e1b5af6820ed7912396dd8c51.jpg");
        addData("http://img5.imgtn.bdimg.com/it/u=961560240,1808896943&fm=21&gp=0.jpg");
        addData("http://img2.imgtn.bdimg.com/it/u=1300376054,3593796916&fm=21&gp=0.jpg");
        addData("http://img3.imgtn.bdimg.com/it/u=1133721782,3832707803&fm=21&gp=0.jpg");
        addData("http://c.hiphotos.baidu.com/image/pic/item/2f738bd4b31c870173cebade237f9e2f0708ff8a.jpg");
        addData("http://f.hiphotos.baidu.com/image/pic/item/622762d0f703918f15e87fcb533d269759eec4f2.jpg");
        addData("http://h.hiphotos.baidu.com/image/pic/item/f31fbe096b63f62494334a038544ebf81a4ca340.jpg");
        addData("http://f.hiphotos.baidu.com/image/pic/item/1c950a7b02087bf4cf813ee9f0d3572c11dfcf45.jpg");
        addData("http://c.hiphotos.baidu.com/image/pic/item/9f510fb30f2442a7fb220ca8d343ad4bd113028a.jpg");
        addData("http://h.hiphotos.baidu.com/image/pic/item/5d6034a85edf8db1ec0d3d6e0a23dd54564e749c.jpg");
        addData("http://f.hiphotos.baidu.com/image/pic/item/8d5494eef01f3a29a8b0ef3e9b25bc315d607cc1.jpg");
        mdatas.addAll(mdatas);
        mdatas.addAll(mdatas);
        mdatas.addAll(mdatas);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        density = getResources().getDisplayMetrics().density;

    }

    private void addData(String url) {
        ItemData data = new ItemData();
        data.key = url;
        data.type = 1;
        mdatas.add(data);
    }
    
    private void addLocalData(){
        
    }

    private void checkBuffer(Bitmap bm) {
        Class clazz = Bitmap.class;
        try {
            Field field = clazz.getDeclaredField("mBuffer");
            field.setAccessible(true);
            Object obj = field.get(bm);
            System.out.println("mBuffer:" + obj);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.center:
                scaleType = ScaleType.CENTER;
                break;
            case R.id.centerinside:
                scaleType = ScaleType.CENTER_INSIDE;
                break;
            case R.id.centercrop:
                scaleType = ScaleType.CENTER_CROP;
                break;
            case R.id.fitxy:
                scaleType = ScaleType.FIT_XY;
                break;
            case R.id.fitstart:
                scaleType = ScaleType.FIT_START;
                break;
            case R.id.fitend:
                scaleType = ScaleType.FIT_END;
                break;
            case R.id.fitcenter:
                scaleType = ScaleType.FIT_CENTER;
                break;
            case R.id.matrixtopcrop:
                scaleType = ScaleType.MATRIX;
                matrixScaleType = MatrixScaleType.TOP_CROP;
                break;
            case R.id.normalDrawer:
                drawerType = DrawerFactory.NORMAL;
                break;
            case R.id.roundrectDrawer:
                drawerType = DrawerFactory.ROUND_RECT;
                break;
            case R.id.circleDrawer:
                drawerType = DrawerFactory.CIRCLE;
                break;
            case R.id.customDrawer:
                drawerType = DrawerFactory.CUSTOM;
                break;

        }
        adapter.notifyDataSetChanged();
    }

    class ItemData {
        public String key;
        public int type;
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mdatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mdatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mdatas.get(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            ItemData data = mdatas.get(position);
            if (convertView == null) {
                View v = getLayoutInflater().inflate(R.layout.view_item, parent, false);
                convertView = v;
            }

            CustomImageView civ = (CustomImageView) convertView.findViewById(R.id.iv);
            ViewGroup.LayoutParams params = civ.getLayoutParams();

            if (position % 2 == 0) {
                params.width = (int) (60 * density);
                params.height = (int) (60 * density);
            } else {
                params.width = (int) (100 * density);
                params.height = (int) (100 * density);
            }
            switch (type) {
                case 0:
                    civ.setImageResource(R.drawable.b);
                    break;
                case 1:
                    CustomImageBuilder builder = civ.getBuilder();
                    if (drawerType == DrawerFactory.CUSTOM) {
                        Path path = new Path();
                        path.moveTo(params.width / 2, 0);
                        path.lineTo(0, params.height / 2);
                        path.lineTo(params.width / 2, params.height);
                        path.lineTo(params.width, params.height / 2);
                        path.lineTo(params.width / 2, 0);
                        path.close();
                        builder.setCustomPath(path);
                        Path borderPath = new Path(path);
                        builder.setCustomBorderPath(borderPath);
                    }
                    builder.setBlankRes(R.drawable.s).setBlankScaleType(scaleType).setBlankDrawerType(drawerType)
                            .setErrorRes(R.drawable.error).setErrorScaleType(scaleType).setErrorDrawerType(drawerType)
                            .setDrawerType(drawerType).setScaleType(scaleType).setMatrixScaleType(matrixScaleType).build().url(data.key);
                    break;
            }

            return convertView;
        }

    }

}
