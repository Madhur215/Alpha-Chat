package com.example.alphachat.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alphachat.Activity.MainActivity;
import com.example.alphachat.Activity.SplashActivity;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView name_text_view = view.findViewById(R.id.user_name_profile_fragment);
        TextView email_text_view = view.findViewById(R.id.user_email_profile_fragment);
        ImageView user_image = view.findViewById(R.id.user_image_profile);
        Button logout_button = view.findViewById(R.id.logout_button);

        user_image.setImageURI(Uri.parse(PrefUtils.getUserImage()));
        name_text_view.setText(PrefUtils.getUserFullName());
        email_text_view.setText(PrefUtils.getUserEmail());

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SplashActivity.class);
                intent.putExtra("mode", "logout");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}
