package com.example.ezyeatsapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private DrawerLayout drawerLayout;  // DrawerLayout initialization
    private ImageView profileImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase authentication and current user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Reference views
        drawerLayout = view.findViewById(R.id.drawer_layout);  // Get DrawerLayout from fragment layout
        profileImage = view.findViewById(R.id.profile_picture);

        // Setup drawer and profile image click behavior
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });

        // Handle Firebase user login status
        if (user == null) {
            // Redirect to LoginActivity if user is not logged in
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            // Load user data from Firebase
            loadUserData();
        }

        return view;
    }

    private void loadUserData() {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        reference.child("username").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    TextView textView = getView().findViewById(R.id.user_details);
                    textView.setText(userName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                TextView textView = getView().findViewById(R.id.user_details);
                textView.setText("Failed to load username");
            }
        });
    }
}

