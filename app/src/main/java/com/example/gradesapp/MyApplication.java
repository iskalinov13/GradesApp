package com.example.gradesapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application {
    boolean opencvManagerInstalled = false;
    boolean permissionCamera = false;
    boolean permissionGetAccounts = false;
    boolean permissionStorage = false;
    List<TestCard> activeTestCards;
    List<String> keysactiveTestCards;
    List<TestCard> testCardsCreator;
    List<String> keystestCardsCreator;
    List<Integer> indexesMyTestCards;

    @Override
    public void onCreate() {
        super.onCreate();
        activeTestCards = new ArrayList<>();
        keysactiveTestCards = new ArrayList<>();
        testCardsCreator = new ArrayList<>();
        keystestCardsCreator = new ArrayList<>();
    }

    public List<TestCard> getActiveTestCards() {
        return activeTestCards;
    }

    public List<String> getKeysActiveTestCards() {
        return keysactiveTestCards;
    }

    public List<Integer> getIndexesMyTestCards() {
        return indexesMyTestCards;
    }

    public List<TestCard> getTestCardsCreator() {
        return testCardsCreator;
    }

    public List<String> getKeystestCardsCreator() {
        return keystestCardsCreator;
    }

    public boolean isOpencvManagerInstalled() {
        return opencvManagerInstalled;
    }

    public void setOpencvManagerInstalled(boolean opencvManagerInstalled) {
        this.opencvManagerInstalled = opencvManagerInstalled;
    }

    public boolean isPermissionCamera() {
        return permissionCamera;
    }

    public void setPermissionCamera(boolean permissionCamera) {
        this.permissionCamera = permissionCamera;
    }

    public boolean isPermissionGetAccounts() {
        return permissionGetAccounts;
    }

    public void setPermissionGetAccounts(boolean permissionGetAccounts) {
        this.permissionGetAccounts = permissionGetAccounts;
    }

    public boolean isPermissionStorage() {
        return permissionStorage;
    }

    public void setPermissionStorage(boolean permissionStorage) {
        this.permissionStorage = permissionStorage;
    }

    public boolean canLoadOpenCV() {
        return opencvManagerInstalled && permissionCamera;
    }

    public String getUser() {
        if (permissionGetAccounts) return getUsername();
        else return "anonymous";
    }

    //https://stackoverflow.com/questions/2727029/how-can-i-get-the-google-username-on-android
    public String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return "Anonymous";
    }
}