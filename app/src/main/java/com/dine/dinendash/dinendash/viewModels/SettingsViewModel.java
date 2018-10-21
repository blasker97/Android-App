package com.dine.dinendash.dinendash.viewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dine.dinendash.dinendash.models.Receipt;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<String> username;

    public SettingsViewModel() {

    }

    public MutableLiveData<String> getUsername() {
        if(username == null) {
            username = new MutableLiveData<>();
        }

        return username;
    }

    public void setUsername(String username) {
        getUsername().setValue(username);
    }
}
