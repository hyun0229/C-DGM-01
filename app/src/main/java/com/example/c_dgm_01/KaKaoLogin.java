package com.example.c_dgm_01;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.AuthApiClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import java.util.UUID;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;


public class KaKaoLogin extends Fragment {
    private View loginBtn, logoutBtn;
    private TextView nickName;
    private ImageView profileImage;
    private DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ka_kao_login, container, false);

        loginBtn = v.findViewById(R.id.login);
        logoutBtn = v.findViewById(R.id.logout);
        nickName = v.findViewById(R.id.nickname);
        profileImage = v.findViewById(R.id.profile);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(requireContext())) {
                    System.out.println("login available in onClick Y");
                    UserApiClient.getInstance().loginWithKakaoTalk(requireContext(), callback);
                }
                // 카카오톡이 없다면 카카오 계정 로그인으로 넘어가서 로그인하게 됨
                else {
                    System.out.println("login available in onClick N");
                    UserApiClient.getInstance().loginWithKakaoAccount(requireContext(), callback);
                }
            }

        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        updateKakaoLoginUi();
                        return null;
                    }
                });
            }

        });

        updateKakaoLoginUi();


        return v;
    }
    private void updateKakaoLoginUi(){ //로그인 여부 확인
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){
                    closeFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().remove(KaKaoLogin.this).commit();

                }
                else   {
                    nickName.setText(null);
                    profileImage.setImageBitmap(null);
                    loginBtn.setVisibility(View.VISIBLE);
                    logoutBtn.setVisibility(View.GONE);

                }
                return null;
            }
        });
    }
    Function2<OAuthToken, Throwable, Unit> callback =
            new Function2<OAuthToken, Throwable, Unit>() {
                @Override
                public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                    if (oAuthToken != null) {
                        System.out.println("token Y");
                    }
                    if (throwable != null) {
                        System.out.println("message s");
                        throwable.printStackTrace();
                        throwable.getMessage();
                        System.out.println("throwable Y");
                    }
                    updateKakaoLoginUi();
                    return null;
                }
            };
    public interface FragmentChangeListener {
        void onFragmentClosed();
    }

    // ...

    private void closeFragment() {
        // Fragment 종료 시
        FragmentChangeListener listener = (FragmentChangeListener) getActivity();
        if (listener != null) {
            listener.onFragmentClosed();
        }
    }

}