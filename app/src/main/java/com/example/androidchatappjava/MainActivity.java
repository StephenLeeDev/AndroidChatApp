package com.example.androidchatappjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.androidchatappjava.Profile.ProfileActivity;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayoutMain;
    ViewPager viewPagerMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayoutMain = findViewById(R.id.tabLayoutMain);
        viewPagerMain = findViewById(R.id.viewPagerMain);

        setViewPager();
    }

    private void setViewPager() {
        tabLayoutMain.addTab(tabLayoutMain.newTab().setCustomView(R.layout.tab_chat));
        tabLayoutMain.addTab(tabLayoutMain.newTab().setCustomView(R.layout.tab_requests));
        tabLayoutMain.addTab(tabLayoutMain.newTab().setCustomView(R.layout.tab_find_friends));

        tabLayoutMain.setTabGravity(TabLayout.GRAVITY_FILL);

        Adapter adapter = new Adapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerMain.setAdapter(adapter);

        tabLayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerMain.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPagerMain.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutMain));
    }


    class Adapter extends FragmentPagerAdapter {

        public Adapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    ChatFragment chatFragment = new ChatFragment();
                    return chatFragment;
                case 1:
                    RequestsFragment requestsFragment = new RequestsFragment();
                    return requestsFragment;
                case 2:
                    FindFriendsFragment findFriendsFragment = new FindFriendsFragment();
                    return findFriendsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabLayoutMain.getTabCount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean doubleBackPressed = false;

    @Override
    public void onBackPressed() {
        if (tabLayoutMain.getSelectedTabPosition() > 0) {
            tabLayoutMain.selectTab(tabLayoutMain.getTabAt(0));
        } else {
            if (doubleBackPressed) {
                finishAffinity();
            } else {
                doubleBackPressed = true;
                Toast.makeText(this, getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show();

                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(() -> {
                    doubleBackPressed = false;
                }, 2000);
            }
        }
    }
}