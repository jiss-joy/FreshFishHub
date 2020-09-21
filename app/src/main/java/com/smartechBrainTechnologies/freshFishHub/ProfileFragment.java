package com.smartechBrainTechnologies.freshFishHub;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView name, number, id, email, logOutBTN;
    private TextView addressBTN, pastOrdersBTN, constactUsBTN, tosBTN, privacyBTN;
    private ProgressDialog mProgress;
    private ImageButton editBTN;
    private TextView toolbarTitle;


    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DocumentReference consumerRef;
    private Map<String, Object> user = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        toolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Profile");

        initValues(view);

        loadData();

        setUpButtons();

        return view;
    }

    private void setUpButtons() {

        editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPage();
            }
        });

        addressBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MyAddressesActivity.class));
            }
        });

        pastOrdersBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PastOrdersActivity.class));
            }
        });

        constactUsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ContactUsActivity.class));
            }
        });

        tosBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        privacyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //https://privacypolicyfreshfishhub.blogspot.com/2020/09/privacy-policy-smartech-brain.html
            }
        });

        logOutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void showEditPage() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.popup_edit_profile);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final EditText name_et = (EditText) dialog.findViewById(R.id.edit_profile_name);
        final EditText phone_et = (EditText) dialog.findViewById(R.id.edit_profile_phone);
        phone_et.setEnabled(false);
        final EditText email_et = (EditText) dialog.findViewById(R.id.edit_profile_email);
        final TextView warning = (TextView) dialog.findViewById(R.id.edit_profile_warning_tv);
        warning.setVisibility(View.GONE);
        name_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning.setVisibility(View.GONE);
                return false;
            }
        });
        email_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning.setVisibility(View.GONE);
                return false;
            }
        });
        ExtendedFloatingActionButton submitBTN = (ExtendedFloatingActionButton) dialog.findViewById(R.id.edit_profile_next);
        consumerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    name_et.setText(task.getResult().getString("consumerName"));
                    phone_et.setText(task.getResult().getString("consumerPhone"));
                    email_et.setText(task.getResult().getString("consumerEmail"));
                }
            }
        });
        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_et.getText().toString();
                String email = email_et.getText().toString();
                if (name.isEmpty()) {
                    warning.setText("Name cannot be empty.");
                    warning.setVisibility(View.VISIBLE);
                } else if (email.isEmpty()) {
                    warning.setText("Email cannot be empty.");
                    warning.setVisibility(View.VISIBLE);
                } else {
                    mProgress.setMessage("Updating User Details ...");
                    mProgress.show();
                    dialog.dismiss();
                    updateData(name, email);
                }
            }
        });
        dialog.show();
    }

    private void updateData(String name, String email) {
        user.put("consumerName", name);
        user.put("consumerEmail", email);
        consumerRef.update(user);
        mProgress.dismiss();
    }

    private void confirm() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.pop_up_logout);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ExtendedFloatingActionButton confirm_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.logout_confirm);
        ExtendedFloatingActionButton cancel_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.logout_cancel);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), AuthenticationBridgeActivity.class));
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void loadData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        consumerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                name.setText(documentSnapshot.getString("consumerName"));
                number.setText(documentSnapshot.getString("consumerPhone"));
                email.setText(documentSnapshot.getString("consumerEmail"));
                id.setText("ID: " + documentSnapshot.getString("consumerID"));
                mProgress.dismiss();
            }
        });
    }

    private void initValues(View view) {
        name = (TextView) view.findViewById(R.id.profile_name);
        number = (TextView) view.findViewById(R.id.profile_number);
        email = (TextView) view.findViewById(R.id.profile_email);
        id = (TextView) view.findViewById(R.id.profile_user_id);
        editBTN = (ImageButton) view.findViewById(R.id.profile_edit_btn);
        addressBTN = (TextView) view.findViewById(R.id.profile_address_btn);
        pastOrdersBTN = (TextView) view.findViewById(R.id.profile_past_orders_btn);
        constactUsBTN = (TextView) view.findViewById(R.id.profile_contact_us_btn);
        tosBTN = (TextView) view.findViewById(R.id.profile_tos_btn);
        privacyBTN = (TextView) view.findViewById(R.id.profile_privacy_btn);
        logOutBTN = (TextView) view.findViewById(R.id.profile_log_out_btn);
        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        consumerRef = db.collection("Consumers").document(currentUser.getUid());
    }
}
