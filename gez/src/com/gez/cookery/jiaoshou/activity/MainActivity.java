package com.gez.cookery.jiaoshou.activity;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gez.cookery.jiaoshou.R;
import com.gez.cookery.jiaoshou.fragments.AboutUsFragment;
import com.gez.cookery.jiaoshou.fragments.FeedBackFragment;
import com.gez.cookery.jiaoshou.fragments.MainFragment;
import com.gez.cookery.jiaoshou.fragments.MainMenuFragment;
import com.gez.cookery.jiaoshou.model.Banb;
import com.gez.cookery.jiaoshou.model.JsonModel;
import com.gez.cookery.jiaoshou.net.IJsonModelData;
import com.gez.cookery.jiaoshou.net.RestClient;
import com.gez.cookery.jiaoshou.service.OrderStateService;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

	private TextView mAddressTextView;

	//当前选中按钮ID
	private int currentContentId;
	private Fragment currentFragment;
	
	private MainFragment mainFragment;
	private AboutUsFragment aboutUsFragment;
	private FeedBackFragment feedBackFragment;
	private Intent intent = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_content);

		
		// 设置滑动菜单
		setBehindContentView(R.layout.menu_frame);
		getSlidingMenu().setSlidingEnabled(true);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);// 设置滑动的屏幕范围，该设置为全屏区域都可以滑动

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);// 显示向左的图标

		View customNav = LayoutInflater.from(this).inflate(R.layout.main_title,
				null);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);

		getSupportActionBar().setCustomView(customNav, params);
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		mAddressTextView = (TextView) customNav
				.findViewById(R.id.main_current_address_text_view);
		mAddressTextView.setText(R.string.main_title);

		// 设置主页面显示的Fragment
		mainFragment = new MainFragment();
		currentFragment = mainFragment;
		this.currentContentId = R.id.home_menu_main_business;
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mainFragment).commit();
		
		// 设置滑动内容
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MainMenuFragment()).commit();

		// 自定义滑动菜单样式
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);// SlidingMenu划出时主页面显示的剩余宽度
		sm.setShadowWidthRes(R.dimen.shadow_width);// 设置阴影图片的宽度
		sm.setShadowDrawable(R.drawable.shadow);// 设置阴影图片
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);// SlidingMenu滑动时的渐变程度
		
		// 启动订单状态自动刷新Service
		startOrderStateService();
		
		// 检查是否是最新版本，提示更新
		checkVersionInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "").setIcon(R.drawable.tp_login)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		// SubMenu subMenu1 = menu.addSubMenu("更多");
		// subMenu1.add(0, 1, 0,"地图模式");
		// subMenu1.add(0, 2, 1,"搜索餐厅");

		// MenuItem subMenu1Item = subMenu1.getItem();
		// subMenu1Item.setIcon(R.drawable.m_drawer_icon_gift_selected);
		// subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
		// MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		case 1:
			showMyAccountCenter();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 切换内容
	 * @param paramInt 
	 * @param clear 是否清除
	 */
	public void showContent(int paramInt, boolean clear) {
		if (this.currentContentId != paramInt || clear) {

			Fragment fragment = null;
			
			if (clear) {
				if (mainFragment != null) {
					if (currentFragment == mainFragment) {
						currentFragment = null;
					}
					
					getSupportFragmentManager().beginTransaction().remove(mainFragment).commit();
				}
			}
			
			switch (paramInt) {
				default:
				case R.id.home_menu_main_business:
						if (mainFragment == null || clear) {
							mainFragment = new MainFragment();
						}
						fragment = mainFragment;
					break;
				case R.id.home_menu_main_feedback:
						if (feedBackFragment == null || clear) {
							feedBackFragment = new FeedBackFragment();
						}
						fragment = feedBackFragment;
					break;
				case R.id.home_menu_main_about_us:
						if (aboutUsFragment == null || clear) {
							aboutUsFragment = new AboutUsFragment();
						}
						fragment = aboutUsFragment;
					break;
				case R.id.home_menu_main_exit:
					stopService(intent);
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
					break;
			}

			if (fragment != null) {
				switchContent(currentFragment, fragment);
			}
		}
		
		currentContentId = paramInt;
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}
	
	private void switchContent(Fragment from, Fragment to) {
		if (from  != null) {
			FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
	        if (!to.isAdded()) {    // 先判断是否被add过
	            transaction.hide(from).add(R.id.content_frame, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
	        } else {
	            transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
	        }
		}
		else {
			FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
			transaction.add(R.id.content_frame, to).commit();
		}
        
        currentFragment = to;
    }

	private void showMyAccountCenter() {
		 Intent intent = new Intent(this, MyAccountActivity.class);
		 startActivity(intent);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
	
	/**
	 * 启动订单状态自动刷新Service
	 */
	private void startOrderStateService() {
		intent = new Intent(this, OrderStateService.class);
		startService(intent);
	}
	
	/**
	 * 检查当前版本信息，提示是否更新
	 */
	private void checkVersionInfo() {
		RestClient.getLatestVersion(new IJsonModelData() {
			@Override
			public void onReturn(JsonModel data) {
				if (data != null) {
					Banb version = (Banb) data;
					String myVersion = null;
					String latestVersion = version.getBanbmc();
					final String downloadUrl = "http://" + version.getXiazdz();
					System.out.println("当前版本号：" + latestVersion);
					System.out.println("下载地址：" + downloadUrl);
					
					try {
						PackageManager manager = MainActivity.this.getPackageManager();
						PackageInfo info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
						myVersion = info.versionName;
						System.out.println("我的版本号：" + myVersion);
						} catch (Exception e) {
							e.printStackTrace();
							}
					
						try {
							if (!latestVersion.equals(myVersion)) {
								Builder builder = new Builder(MainActivity.this);
								builder.setIcon(R.drawable.alert_dialog_icon);
								builder.setTitle("更新提示");
								builder.setMessage("发现新版本，是否升级 ？");

								builder.setPositiveButton("否", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});

								builder.setNegativeButton("是", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										// 下载新版本
										Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)); 
										startActivity(it);
									}
								});
								builder.create().show();
								}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
		});
	}
}