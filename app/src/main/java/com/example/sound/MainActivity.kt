package com.example.sound

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.core.view.ViewCompat
import com.example.sound.databinding.ActivityMainBinding
import com.example.sound.ui.fragment.TabsAdapter
import com.google.android.material.tabs.TabLayout

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 清除数据库
        // MyApplication.context.deleteDatabase("SoundData.db")
        // 设置状态栏字体颜色，深色主题下设置为白色；浅色主题下设置为黑色
        setAndroidNativeLightStatusBar()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("检测").setIcon(R.drawable.microphone))
        tabLayout.addTab(tabLayout.newTab().setText("记录").setIcon(R.drawable.history))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = TabsAdapter(this, supportFragmentManager, tabLayout.tabCount)
        val viewPager = binding.viewPager
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    // 跟随深色浅色模式变化状态栏字体颜色
    private fun Activity.setAndroidNativeLightStatusBar() {
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.isAppearanceLightStatusBars = !isDarkMode()
    }

    // 判断是否处于深色模式
    private fun Context.isDarkMode(): Boolean {
        return resources.configuration.uiMode == 0x21
    }

}
