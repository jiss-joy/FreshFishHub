package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    private TextView addressBTN, pastOrdersBTN, contactUsBTN, tosBTN, privacyBTN;
    private ProgressDialog mProgress;
    private ImageButton editBTN;
    private TextView toolbarTitle, version;


    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DocumentReference consumerRef;
    private Map<String, Object> user = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        toolbarTitle = view.findViewById(R.id.toolbar_title);
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
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
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

        contactUsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ContactUsActivity.class));
            }
        });

        tosBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/e/2PACX-1vRESRvYZHUdn1Nld146Gx9Ne2-xwcLOoy0L07qU0zLqF-57MCryO8tDFCLZ8sRBlw-75BR1aBhMqBH2/pub")));
            }
        });

        privacyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/e/2PACX-1vSUcHQZ2sucQs2pIkKF66a1vXYGvgMKImAM1WisrhajEzj1SsKpHwy0czAGhODe64PEAAfc2xIYvZpd/pub")));
            }
        });

        logOutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

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
        ExtendedFloatingActionButton confirm_btn = dialog.findViewById(R.id.logout_confirm);
        ExtendedFloatingActionButton cancel_btn = dialog.findViewById(R.id.logout_cancel);
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
        name = view.findViewById(R.id.profile_name);
        number = view.findViewById(R.id.profile_number);
        email = view.findViewById(R.id.profile_email);
        id = view.findViewById(R.id.profile_user_id);
        editBTN = view.findViewById(R.id.profile_edit_btn);
        addressBTN = view.findViewById(R.id.profile_address_btn);
        pastOrdersBTN = view.findViewById(R.id.profile_past_orders_btn);
        contactUsBTN = view.findViewById(R.id.profile_contact_us_btn);
        tosBTN = view.findViewById(R.id.profile_tos_btn);
        version = view.findViewById(R.id.profile_version);
        privacyBTN = view.findViewById(R.id.profile_privacy_btn);
        logOutBTN = view.findViewById(R.id.profile_log_out_btn);
        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);

        version.setText("Version: " + BuildConfig.VERSION_NAME);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        consumerRef = db.collection("Consumers").document(currentUser.getUid());
    }

}
