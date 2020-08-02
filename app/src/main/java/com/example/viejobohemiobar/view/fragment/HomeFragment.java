package com.example.viejobohemiobar.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.viejobohemiobar.R;
import com.example.viejobohemiobar.view.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1 = "Hello";
    private listener listener;

    @BindView(R.id.buttonScan)
    Button buttonScan;
    @BindView(R.id.textViewHome)
    TextView textViewHome;


    //TODO BORRAR

    @BindView(R.id.buttonSkip)
    Button getButtonSkip;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (listener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        MainActivity main = (MainActivity) getActivity();
        main.toolbar.getMenu().findItem(R.id.itemToolbarYourOrder).setVisible(false);
//        main.resetOrder();


        textViewHome.setText(mParam1);

        //TODO BORRAR
        getButtonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.homeListener(false);
            }
        });

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.homeListener(true);
            }
        });

        return view;
    }

    public interface listener{
        void homeListener(Boolean boo);
    }
}