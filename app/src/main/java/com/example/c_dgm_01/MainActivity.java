package com.example.c_dgm_01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,KaKaoLogin.FragmentChangeListener {
    RecyclerView recyclerView;
    TextView welcomeTextView,profile_name,profile_email;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView profileImage;
    Button logoutBTN;
    int flag=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KakaoSdk.init(this,"f9cbabbee252438dbcc8e284586eea39");
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        });

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            test();
            welcomeTextView.setVisibility(View.GONE);
            flag=1;

        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //로그인

        View headerView=navigationView.getHeaderView(0);
        logoutBTN = headerView.findViewById(R.id.logout);
        profileImage =headerView.findViewById(R.id.profile_pic);
        profile_name= headerView.findViewById(R.id.profile_name);


        updateKakaoLoginUi();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close); //토글설정
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
    }

    @Override
    public void onBackPressed () { // DrawerLayout 열고 닫고
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected (@NonNull MenuItem item){
        int itemId = item.getItemId();
        if (itemId == R.id.nav_item1) {
            Toast.makeText(this, "주변대피소", Toast.LENGTH_SHORT).show();
            NaverFragment naverFragment = new NaverFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.bottom_layout,naverFragment,"naver_fragment").commit();
            welcomeTextView.setVisibility(View.GONE);
        } else if (itemId == R.id.nav_item2) {
            Toast.makeText(this, "다른 항목", Toast.LENGTH_SHORT).show();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("naver_fragment");
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            if (flag == 0) {
                welcomeTextView.setVisibility(View.VISIBLE);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }




    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }

    public void test(){
        messageList.add(new Message("입력중... ",Message.SENT_BY_BOT));
        addResponse("답장입니다");

    }
    private void updateKakaoLoginUi(){ //로그인 여부 확인
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){
                    profile_name.setText(user.getKakaoAccount().getProfile().getNickname());
                    Glide.with(profileImage).load(user.getKakaoAccount().getProfile().getThumbnailImageUrl()).circleCrop().into(profileImage); //프로필이미지에 넣기
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                else   {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    KaKaoLogin kaKaoLogin  = new KaKaoLogin();
                    getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout,kaKaoLogin,"kakao_fragment").commit();

                }
                return null;
            }
        });
    }
    public void onLogoutButtonClick(View view) {
        UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
            @Override
            public Unit invoke(Throwable throwable) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                KaKaoLogin kaKaoLogin  = new KaKaoLogin();
                getSupportFragmentManager().beginTransaction().replace(R.id.drawer_layout,kaKaoLogin,"kakao_fragment").commit();
                return null;
            }
        });
    }
    @Override
    public void onFragmentClosed() {
        updateKakaoLoginUi();
    }

}