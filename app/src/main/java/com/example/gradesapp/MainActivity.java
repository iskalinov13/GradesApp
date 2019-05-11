package com.example.gradesapp;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    AdapterTestCard adapterCardTest;////
    FirebaseDatabase database;
    List<TestCard> activeTestCards;////
    List<String> keysactiveTestCards;
    List<TestCard> testCardsCreator;
    List<String> keystestCardsCreator;
    TabLayout tabLayout;
    View view;
    ActionMode actionMode;
    private static final int APPLICATION_PERMISSION_TO_OBTAIN_ACCOUNTS = 2;
    Menu menu;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       activeTestCards = ((MyApplication) this.getApplication()).getActiveTestCards();
       keysactiveTestCards = ((MyApplication) this.getApplication()).getKeysActiveTestCards();
       testCardsCreator = ((MyApplication) this.getApplication()).getTestCardsCreator();
       keystestCardsCreator = ((MyApplication) this.getApplication()).getKeystestCardsCreator();
        setContentView(R.layout.activity_main);///
        view = findViewById(R.id.content_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (actionMode != null) actionMode.finish();
                switch (tab.getPosition()) {
                    case 0:
                        menu.findItem(R.id.create).setEnabled(false);//
                        menu.findItem(R.id.create).setVisible(false);////////
                        adapterCardTest.setFilterCard(false);
                        break;
                    case 1:
                        menu.findItem(R.id.create).setEnabled(true);//////
                        menu.findItem(R.id.create).setVisible(true);/////
                        adapterCardTest.setFilterCard(true);//////
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_listado);
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapterCardTest = new AdapterTestCard(activeTestCards, testCardsCreator);

        adapterCardTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                String solution = getTestCard(v).getSolution();
                String title = getTestCard(v).getTitle();
                i.putExtra("action", "correct");
                i.putExtra("title", title);
                i.putExtra("solution", solution);
                startActivity(i);
            }
        });

        adapterCardTest.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (tabLayout.getSelectedTabPosition() == 1){
                    System.out.println("1 basyldy__________________...........................<<<<<<<<<<<<<<<<<<<<<<<");
                    actionMode = startActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                           // mode.setTitle("Selected test");
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.menu_context, menu);//////////
                            v.setBackground(getResources().getDrawable(R.drawable.border_highlight));
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.active:////////
                                    String key = getKeyTestCard(v);
                                    if (getTestCard(v).isActive()){
                                        changeActivationTestCard(key, !getTestCard(v).isActive());
                                        showSnakcbar("Test not active");
                                    }
                                    else {
                                        changeActivationTestCard(key, !getTestCard(v).isActive());
                                        showSnakcbar("Test active");
                                    }
                                    mode.finish();
                                    break;
                                case R.id.edit://////
                                    Intent i = new Intent(MainActivity.this, ExamDetailActivity.class);
                                    i.putExtra("action", "edit");
                                    key = getKeyTestCard(v);
                                    i.putExtra("key", key);
                                    startActivity(i);
                                    mode.finish();
                                    break;
                                case R.id.update:
                                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                                    intent.putExtra("action", "update");
                                    key = getKeyTestCard(v);
                                    intent.putExtra("key", key);
                                    startActivity(intent);
                                    break;
                                case R.id.remove://///
                                    key = getKeyTestCard(v);
                                    confirmeliminationTest(key);
                                    mode.finish();
                                    break;
                                default:
                                    return false;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            v.setBackground(getResources().getDrawable(R.drawable.border));
                        }
                    });}
                return true;
            }
        });
        mRecyclerView.setAdapter(adapterCardTest);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(this);
        preLeerBBDD(Manifest.permission.GET_ACCOUNTS, APPLICATION_PERMISSION_TO_OBTAIN_ACCOUNTS);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void removeTestCard(String key) {
        database = FirebaseDatabase.getInstance();
        database.getReference("tests/" + key).removeValue();
    }

    void changeActivationTestCard(String key, boolean active) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("tests/" + key);
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("active", active);
        databaseReference.updateChildren(taskMap);
    }

    TestCard getTestCard(View v) {
        TestCard testCard;
        int index = mRecyclerView.getChildAdapterPosition(v);
        if (adapterCardTest.isFilterCard()) {
            testCard = ((MyApplication) getApplication()).getTestCardsCreator().get(index);

        } else {
            testCard = ((MyApplication) getApplication()).getActiveTestCards().get(index);
        }
        return testCard;
    }

    String getKeyTestCard(View v) {
        String key;
        int index = mRecyclerView.getChildAdapterPosition(v);
        if (adapterCardTest.isFilterCard()) {
            key = ((MyApplication) getApplication()).getKeystestCardsCreator().get(index);

        } else {
            key = ((MyApplication) getApplication()).getKeysActiveTestCards().get(index);
        }
        return key;
    }

    void leerBBDD() {
        database = FirebaseDatabase.getInstance();
        database.getReference("tests").orderByChild("active").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.VISIBLE);
                        activeTestCards.removeAll(activeTestCards);
                        keysactiveTestCards.removeAll(keysactiveTestCards);
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            TestCard testCard = childDataSnapshot.getValue(TestCard.class);
                            activeTestCards.add(testCard);
                            String keyTestCard = childDataSnapshot.getKey();
                            keysactiveTestCards.add(keyTestCard);
                        }
                        adapterCardTest.notifyDataSetChanged();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
        database.getReference("tests").orderByChild("creator").equalTo(((MyApplication) getApplication()).getUser())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        testCardsCreator.removeAll(testCardsCreator);
                        keystestCardsCreator.removeAll(keystestCardsCreator);
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            TestCard testCard = childDataSnapshot.getValue(TestCard.class);
                            testCardsCreator.add(testCard);
                            String keyTestCard = childDataSnapshot.getKey();
                            keystestCardsCreator.add(keyTestCard);
                        }
                        adapterCardTest.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

    }

    public void preLeerBBDD(final String permission, final int request) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            ((MyApplication) getApplication()).setPermissionGetAccounts(true);
            leerBBDD();
        }
        else {
            ((MyApplication) getApplication()).setPermissionGetAccounts(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Snackbar snackbar = make(view, "Permission is needed to obtain the accounts", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, request);
                            }
                        }).setActionTextColor(getResources().getColor(R.color.colorAccent));
                snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, request);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case APPLICATION_PERMISSION_TO_OBTAIN_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    leerBBDD();
                } else {
                    leerBBDD();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);/////////
        this.menu = menu;
        menu.findItem(R.id.create).setEnabled(false);/////
        menu.findItem(R.id.create).setVisible(false);//////
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create:///////
                throwCameraActivity();//////
                break;
            case R.id.aboutUs:///////
                throwAboutUsActivity();
                break;
            case R.id.tutorial:///////
                throwWelcomeActivity();//////
                break;
        }
        return true;
    }

    void throwCameraActivity() {
        Intent i = new Intent(MainActivity.this, CameraActivity.class);
        i.putExtra("action", "create");
        startActivity(i);
    }
    void throwWelcomeActivity() {
        Intent i = new Intent(MainActivity.this, MaterialAbout.class);
        i.putExtra("action", "tutorial");
        startActivity(i);
    }
    void throwAboutUsActivity() {
        Intent i = new Intent(MainActivity.this, AboutUsActivity.class);
        i.putExtra("action", "aboutUs");
        startActivity(i);
    }

    //https://stackoverflow.com/questions/11740311/android-confirmation-message-for-delete
    private void confirmeliminationTest(final String key) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Remove")
                .setMessage("Do you want to delete this test?")
                .setIcon(R.drawable.ic_remove_circle)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        removeTestCard(key);
                        showSnakcbar("Test is removed");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    void showSnakcbar(String message){
        Snackbar snackbar = make(view, message, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {}
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }
}

