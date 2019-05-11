package com.example.gradesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExamDetailActivity extends AppCompatActivity {
    Switch switchActive;
    EditText editTextTitle;
    EditText editTextDescription;
    EditText textViewNumberOfQuestions;
    EditText textViewCreator;
    EditText textViewCreationDate;
    String key = null;
    int NumberOfQuestions;
    long CreationDate;
    String solution;
    View view;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_card);/////
        view = findViewById(R.id.content_main);/////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        switchActive = (Switch) findViewById(R.id.switchActive);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        textViewNumberOfQuestions = (EditText) findViewById(R.id.editTextViewNumberOfQuestions);
        textViewCreator = (EditText) findViewById(R.id.editTextViewCreate);
        textViewCreationDate = (EditText) findViewById(R.id.editTextCreationDate);
        manageAction();
    }

    private void manageAction() {
        action = getIntent().getExtras().getString("action");
        switch (action) {
            case "edit":
                actionEditor();
                break;
            case "save":
                actionSave();
                break;
            case "update":
                actionUpdate();
                break;
        }
    }

    private void actionEditor() {
        key = getIntent().getExtras().getString("key");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("tests/" + key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TestCard testCard = dataSnapshot.getValue(TestCard.class);
                if (testCard != null) {
                    solution = testCard.getSolution();
                    switchActive.setChecked(testCard.isActive());
                    editTextTitle.setText(testCard.getTitle());
                    editTextDescription.setText(testCard.getDescription());
                    textViewNumberOfQuestions.setText("" + testCard.getNumberOfQuestions());
                    textViewCreator.setText(testCard.getCreator());
                    textViewCreationDate.setText("" + getDate(testCard.getCreationDate()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("action", "update");
        bundle.putString("title", editTextTitle.getText().toString());
        bundle.putString("description", editTextDescription.getText().toString());
        bundle.putString("creationDate", textViewCreationDate.getText().toString());
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (bundle != null) {
            editTextTitle.setText(bundle.getString("title"));
            editTextDescription.setText(bundle.getString("description"));
            textViewCreationDate.setText(bundle.getString("creationDate"));
        }
    }

    private void actionSave() {
        Bundle extras = getIntent().getExtras();
        solution = extras.getString("solution");
        NumberOfQuestions = extras.getInt("numberOfQuestions");
        CreationDate = extras.getLong("creationDate");
        switchActive.setChecked(false);
        editTextTitle.setText("");
        editTextDescription.setText("");
        textViewNumberOfQuestions.setText("" + NumberOfQuestions);
        textViewCreator.setText(((MyApplication) getApplication()).getUser());
        textViewCreationDate.setText("" + getDate(CreationDate));
    }

    private void actionUpdate() {
        key = getIntent().getExtras().getString("key");
        solution = getIntent().getExtras().getString("solution");
        NumberOfQuestions = getIntent().getExtras().getInt("numberOfQuestions");
        textViewNumberOfQuestions.setText("" + NumberOfQuestions);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("tests/" + key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TestCard testCard = dataSnapshot.getValue(TestCard.class);
                if (testCard != null) {
                    switchActive.setChecked(testCard.isActive());
                    editTextTitle.setText(testCard.getTitle());
                    editTextDescription.setText(testCard.getDescription());
                    textViewCreator.setText(testCard.getCreator());
                    textViewCreationDate.setText("" + getDate(testCard.getCreationDate()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_test_card, menu);////////
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save://////
                save();
                break;
            case R.id.cancel://////
                finish();
                break;
        }
        return true;
    }

    void launchListinig() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    void save() {
        if (!checkForm()) return;
        if (key == null) key = saveTestCard();
        else updateTestCard();
        hideVirtualKeyboard();
        Snackbar snackbar = Snackbar.make(view, "Test is saved", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchListinig();
            }
        }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }
    void hideVirtualKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    String saveTestCard() {
        TestCard testCard = new TestCard.TestCardBuilder()
                .isActive(switchActive.isChecked())
                .withtitle(editTextTitle.getText().toString())
                .withDescription(editTextDescription.getText().toString())
                .withCreator(textViewCreator.getText().toString())
                .withnumberOfQuestions(NumberOfQuestions)
                .withCreationDate(CreationDate)
                .withSolution(solution)
                .build();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("tests");
        DatabaseReference newPostRef = databaseReference.push();
        newPostRef.setValue(testCard);
        return newPostRef.getKey();
    }

    void updateTestCard() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("tests/" + key);
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("active", switchActive.isChecked());
        taskMap.put("title", editTextTitle.getText().toString());
        taskMap.put("description", editTextDescription.getText().toString());
        taskMap.put("numberOfQuestions", Integer.parseInt(textViewNumberOfQuestions.getText().toString()));
        taskMap.put("solution", solution);
        databaseReference.updateChildren(taskMap);
    }

    boolean checkForm() {
        boolean revision = true;
        if (isEmpty(editTextTitle)) {
            Toast.makeText(ExamDetailActivity.this, "It is necessary to include a title.", Toast.LENGTH_LONG).show();
            revision = false;
        }
        if (isEmpty(editTextDescription)) {
            Toast.makeText(ExamDetailActivity.this, "It is necessary to include a description.", Toast.LENGTH_LONG).show();
            revision = false;
        }
        return revision;
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
}
