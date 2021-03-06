package app.jiyi.com.mjoke.aty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.telly.floatingaction.FloatingAction;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.jiyi.com.mjoke.App;
import app.jiyi.com.mjoke.MyConfig;
import app.jiyi.com.mjoke.R;
import app.jiyi.com.mjoke.fragment.Tab01Fragment;
import app.jiyi.com.mjoke.fragment.Tab02Fragment;
import app.jiyi.com.mjoke.fragment.Tab03Fragment;
import app.jiyi.com.mjoke.fragment.Tab04Fragment;

/**
 * 这个是app的主界面
 * 从欢迎界面到此界面
 * MainActivity--->this
 */
public class AtyMainView extends BaseActivity implements View.OnClickListener{

    private int mCurrentPage=0;

    private ViewPager mViewPager;
    private List<Fragment> mDatas;
    private FragmentPagerAdapter mAdapter;

    private TextView tv_tab[]=new TextView[4];//ViewPager数组
    private View tabline;//页面上的横线
    private int tabWidth;//横线的宽度
    //标题栏上弹出菜单
    private PopupWindow mPopWin;

    private App mapp;
    private ImageView iv_header;
    private FrameLayout tab_title01;

    private ImageView iv_main_edit;

    //悬浮按钮
    private ListView mlistview;
    private FloatingAction mFloatingAction;
    private FloatingAction.Builder mbuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aty_main_view);
        setImmerseLayout(findViewById(R.id.tab_title01));
        mapp=App.getAppInstance();

        initView();
        initOthersView();
        loadHeader();
    }

    //初始化整体view
    private void initView() {
        mlistview= (ListView) findViewById(R.id.main_lv);
        mbuilder= FloatingAction.from(this)
                .listenTo(mlistview)
                .icon(R.mipmap.pencil)
                .colorResId(R.color.maincolor)
                .listener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PublishActivity.enterPublishAty(AtyMainView.this);
                    }
                });
        mFloatingAction=mbuilder.build();
        tab_title01= (FrameLayout) findViewById(R.id.tab_title01);
        mViewPager= (ViewPager) findViewById(R.id.tab_viewpager);
        tv_tab[0]= (TextView) findViewById(R.id.tv_tab01);
        tv_tab[1]= (TextView) findViewById(R.id.tv_tab02);
        tv_tab[2]= (TextView) findViewById(R.id.tv_tab03);
        tv_tab[3]= (TextView) findViewById(R.id.tv_tab04);
        tabline=findViewById(R.id.tab_line);

        initTabline();//设置tabline的初始宽度
        setAllTextColorOrigin();//将所有文本设置为未点击颜色
        setTextColor(mCurrentPage);

        Tab01Fragment tab01=new Tab01Fragment();
        Tab02Fragment tab02=new Tab02Fragment();
        Tab03Fragment tab03=new Tab03Fragment();
        Tab04Fragment tab04=new Tab04Fragment();
        mDatas=new ArrayList<Fragment>();
        mDatas.add(tab01);
        mDatas.add(tab02);
        mDatas.add(tab03);
        mDatas.add(tab04);

        mAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mDatas.get(position);
            }

            @Override
            public int getCount() {
                return mDatas.size();
            }
        };
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabline.setTranslationX((positionOffset+position)*tabWidth);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage=position;
                setAllTextColorOrigin();//先将全部设置为默认颜色
                setTextColor(position);//再将下一个设置为需要颜色
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initOthersView(){
        iv_header= (ImageView) findViewById(R.id.mainview_userhead);
        iv_header.setOnClickListener(this);
    }
    //加载用户的头像
    private void loadHeader(){
        if(mapp.getisLogin()) {
            String url = MyConfig.BASE_IMG + "/" + mapp.getUserToken() + ".png";
            ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    iv_header.setImageBitmap(bitmap);
                }
            }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    String imgpath = Environment.getExternalStorageDirectory() + "/" + MyConfig.BASE_DIR_NAME + "/" + mapp.getUserToken() + ".png";
                    File f = new File(imgpath);
                    if (f.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
                        iv_header.setImageBitmap(bitmap);
                    } else {
                        iv_header.setImageResource(R.mipmap.user_big_icon);
                    }
                }
            });
            mapp.getQueues().add(imageRequest);
        }else{
            iv_header.setImageResource(R.mipmap.user_big_icon);
        }
    }

    //设置tabline的初始宽度
    private void initTabline() {
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int ScreenWidth=metrics.widthPixels;
        tabWidth=ScreenWidth/tv_tab.length;

        ViewGroup.LayoutParams lp=tabline.getLayoutParams();
        lp.width=tabWidth;
    }

    private void setTextColor(int position){
        if(position<tv_tab.length) {
            tv_tab[position].setTextColor(getResources().getColor(R.color.maincolor));
        }
    }

    //将textView颜色设置原始色
    private void setAllTextColorOrigin(){
        for (int i=0;i<tv_tab.length;i++){
            tv_tab[i].setTextColor(getResources().getColor(R.color.maincolor_off));
        }
    }

    //tab栏的点击事件
    public void tabClick(View view){
        switch (view.getId()){
            case R.id.rl_tab01:
                if(mCurrentPage!=0){
                    mViewPager.setCurrentItem(0);
                    mCurrentPage=0;
                }else{
                    if(mDatas!=null&&mDatas.size()>0) {
                        Tab01Fragment t1f = (Tab01Fragment) mDatas.get(0);
                        t1f.refreshForAct();
                    }
                }
                break;
            case R.id.rl_tab02:
                if(mCurrentPage!=1){
                    mViewPager.setCurrentItem(1);
                    mCurrentPage=1;
                }else{
                    if(mDatas!=null&&mDatas.size()>0) {
                        Tab02Fragment t2f = (Tab02Fragment) mDatas.get(1);
                        t2f.refreshForAct();
                    }
                }
                break;
            case R.id.rl_tab03:
                if(mCurrentPage!=2){
                    mViewPager.setCurrentItem(2);
                    mCurrentPage=2;
                }else{
                    if(mDatas!=null&&mDatas.size()>0) {
                        Tab03Fragment t3f = (Tab03Fragment) mDatas.get(2);
                        t3f.refreshForAct();
                    }
                }
                break;
            case R.id.rl_tab04:
                if(mCurrentPage!=3){
                    mViewPager.setCurrentItem(3);
                    mCurrentPage=3;
                }else{
                    if(mDatas!=null&&mDatas.size()>0) {
                        Tab04Fragment t4f = (Tab04Fragment) mDatas.get(3);
                        t4f.refreshForAct();
                    }
                }
                break;
        }

    }

    //title点击事件
    public void titleButtonClick(View view){
        switch (view.getId()){
            case R.id.title_rl_me://个人中心
//                Toast.makeText(AtyMainView.this,"个人中心",Toast.LENGTH_SHORT).show();
                AtyPersonCenter.enterAtyPersonCenter(AtyMainView.this);
                break;
            case R.id.title_rl_menu://弹出菜单
                if (mPopWin != null&&mPopWin.isShowing()) {
                    mPopWin.dismiss();
                    return;
                 } else {
                    initPopWindow();
                    int fiftypx=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80,
                            getResources().getDisplayMetrics());
//                    Toast.makeText(AtyMainView.this,"fifty:"+fiftypx,Toast.LENGTH_SHORT).show();
                    mPopWin.showAsDropDown(view, -fiftypx, 5);//展示在哪个控件下
//                    mPopWin.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 10,10);
                }
                break;
        }
    }

    //初始化弹窗菜单
    private void initPopWindow(){
        View mPopView= LayoutInflater.from(AtyMainView.this).inflate(R.layout.popmenu_layout,null);

        mPopWin=new PopupWindow(mPopView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置动画效果 [R.style.AnimationFade 是自己事先定义好的]
//        mPopWin.setAnimationStyle(R.style.AnimationFade);
//        ColorDrawable cd = new ColorDrawable(-0000);
        ColorDrawable cd = new ColorDrawable(0x000000);
        mPopWin.setBackgroundDrawable(cd);
        mPopWin.setFocusable(true);
        mPopWin.setOutsideTouchable(true);
        mPopWin.update();

        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha = 0.8f;
        getWindow().setAttributes(lp);

        mPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp=getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

    }


    //弹出菜单点击事件，在布局里设置好了
    public void popMenuItemClick(View view){
        String stip=null;
        if (mPopWin != null&&mPopWin.isShowing()) {
            mPopWin.dismiss();
        }
        switch (view.getId()){
            case R.id.popmenu_item_set:
                SettingActivity.enterSettingAty(AtyMainView.this);
                break;
            case R.id.popmenu_item_refresh:
                if(mCurrentPage==0){
                    Tab01Fragment t1f= (Tab01Fragment) mDatas.get(0);
                    t1f.refreshForAct();
                }else if(mCurrentPage==1){
                    Tab02Fragment t2f= (Tab02Fragment) mDatas.get(1);
                    t2f.refreshForAct();
                }else if(mCurrentPage==2){
                    Tab03Fragment t3f= (Tab03Fragment) mDatas.get(2);
                    t3f.refreshForAct();
                }else if(mCurrentPage==3){
                    Tab04Fragment t4f= (Tab04Fragment) mDatas.get(3);
                    t4f.refreshForAct();
                }
                break;
            case R.id.popmenu_item_edit:
                PublishActivity.enterPublishAty(AtyMainView.this);
                break;
            case R.id.popmenu_item_search:
                DownLoadActivity.enterDownLoadAty(AtyMainView.this);
                break;
            case R.id.popmenu_item_email:
                SendQuesActivity.enterSendQuesAty(AtyMainView.this);
                break;
        }

    };


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mainview_userhead:
                AtyPersonCenter.enterAtyPersonCenter(AtyMainView.this);
                break;
        }
    }

    @Override
    protected void onResume() {
//        if(MyUtils.isBackground(AtyMainView.this)){
//            this.finish();
//        }else{
            loadHeader();
            if(tab_title01!=null){
                tab_title01.setBackgroundColor(mapp.getThemeColor());
            }
//        }
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private long exitTime=0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            MobclickAgent.onKillProcess(this);
            finish();
            System.exit(0);
        }
    }
}
