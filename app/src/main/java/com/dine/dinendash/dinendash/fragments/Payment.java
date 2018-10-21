package com.dine.dinendash.dinendash.fragments;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dine.dinendash.dinendash.R;
import com.dine.dinendash.dinendash.databinding.FragmentPaymentBinding;
import com.dine.dinendash.dinendash.util.Statics;
import com.dine.dinendash.dinendash.viewModels.NewReceiptViewModel;

public class Payment extends Fragment {
    private NewReceiptViewModel viewModel;
    private String amount;

    public Payment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity()!=null) {
            viewModel = ViewModelProviders.of(getActivity()).get(NewReceiptViewModel.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPaymentBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment, container, false);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);
        binding.setFragment(this);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void chooseContact() {
        Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    Statics.REQUEST_CODE_ASK_PERMISSIONS);
        }
        else {
            startActivityForResult(intent, Statics.PICK_CONTACT);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();

            Cursor cursor;  // Cursor object
            String mime;    // MIME type
            int dataIdx;    // Index of DATA1 column
            int mimeIdx;    // Index of MIMETYPE column
            int nameIdx;    // Index of DISPLAY_NAME column

            String name = "";
            String phone = "";

            // Get the name
            cursor = getActivity().getContentResolver().query(contactData,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                    null, null, null);
            if (cursor.moveToFirst()) {
                nameIdx = cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME);
                name = cursor.getString(nameIdx);

                // Set up the projection
                String[] projection = {
                        ContactsContract.Data.DISPLAY_NAME,
                        ContactsContract.Contacts.Data.DATA1,
                        ContactsContract.Contacts.Data.MIMETYPE};

                // Query ContactsContract.Data
                cursor = getActivity().getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI, projection,
                        ContactsContract.Data.DISPLAY_NAME + " = ?",
                        new String[]{name},
                        null);

                if (cursor.moveToFirst()) {
                    // Get the indexes of the MIME type and data
                    mimeIdx = cursor.getColumnIndex(
                            ContactsContract.Contacts.Data.MIMETYPE);
                    dataIdx = cursor.getColumnIndex(
                            ContactsContract.Contacts.Data.DATA1);

                    // Match the data to the MIME type, store in variables
                    do {
                        mime = cursor.getString(mimeIdx);
                        if (ContactsContract.CommonDataKinds.Phone
                                .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                            phone = cursor.getString(dataIdx);
                        }
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
            sendMessageTo(name,phone,amount);
        }
    }

    private void sendMessageTo(String name, String phone, String amount) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String username = sharedPreferences.getString("username", "");

        Uri uri = Uri.parse("smsto:"+phone );
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", "Hi "+name+"! You owe me" + amount +" dollars. You can pay me using this link:https://www.paypal.me/"+username+"/20");
        startActivity(it);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseContact();
        }
        else {
            Toast.makeText(getActivity(),"This application requires the use of contacts", Toast.LENGTH_SHORT).show();
        }
    }
}