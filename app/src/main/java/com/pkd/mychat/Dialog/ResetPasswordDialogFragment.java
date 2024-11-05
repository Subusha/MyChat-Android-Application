package com.pkd.mychat.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.pkd.mychat.R;

public class ResetPasswordDialogFragment extends DialogFragment {

    EditText txtEmail;
    Button btnSendRestEmail, btnCancel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_reset_password, null);

        txtEmail = view.findViewById(R.id.txt_restPwEmail);
        btnSendRestEmail = view.findViewById(R.id.btn_resetPassword);
        btnCancel = view.findViewById(R.id.btn_cancelRestPassword);
        btnCancel.setVisibility(View.GONE);

        btnSendRestEmail.setOnClickListener(listener -> {
            resetPassword(txtEmail.getText().toString());
        });

        btnCancel.setOnClickListener(listener -> {
            this.dismiss();
        });

        builder.setView(view).setTitle("Reset Password");

        return builder.create();
    }

    private void resetPassword(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Password reset failed. Please check your email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}