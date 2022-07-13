package com.anubhav.assignmentsfeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String currentUserID, currentUserFullName;
    private DatabaseReference mUsersReference, mAssignmentsListReference, mFullNameToIDReference, mMyProfessorsReference, mMyStudentsReference;

    private AgendaCalendarView mAgendaCalendarView;
    private CardView nothingToSeeNowMessage;
    private FloatingActionButton addEventButton;
    private ImageButton optionsButton;
    private ProgressBar progressBar;
    private TextView homeHeader;
    private HashMap<String, Integer> hashMap = new HashMap<>();
    private ArrayList<String> assignmentIDList = new ArrayList<>();
    private ArrayList<String> fullNamesList = new ArrayList<>();
    private List<CalendarEvent> eventList = new ArrayList<>();

    private int index = 0;
    private long mID = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAssignmentsListReference = FirebaseDatabase.getInstance().getReference().child("Assignments List");
        mFullNameToIDReference = FirebaseDatabase.getInstance().getReference().child("Full Name to ID");
        mMyProfessorsReference = FirebaseDatabase.getInstance().getReference().child("My Professors");
        mMyStudentsReference = FirebaseDatabase.getInstance().getReference().child("My Students");

        homeHeader = view.findViewById(R.id.currentMonth);
        nothingToSeeNowMessage = view.findViewById(R.id.homeCardViewNothingToShow);
        optionsButton = view.findViewById(R.id.optionsButton);
        progressBar = view.findViewById(R.id.progressBar);

        optionsButton.setOnClickListener(optionsView -> showOptions(optionsView));

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.add(Calendar.YEAR, -1);
        maxDate.add(Calendar.YEAR, 1);

        if (currentUserID != null) {
            mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("fullName"))
                        currentUserFullName = snapshot.child("fullName").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        mAssignmentsListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String fullName = childSnapshot.child("fullName").getValue().toString();
                    if (! fullNamesList.contains(fullName))
                        fullNamesList.add(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        CalendarPickerController mPickerController = new CalendarPickerController() {
            @Override
            public void onDaySelected(DayItem dayItem) {

            }

            @Override
            public void onEventSelected(CalendarEvent event) {
                if (! event.getTitle().equals("No events"))
                    sendUserToDetailsActivity(event);
            }

            @Override
            public void onScrollToDate(Calendar calendar) {
                int numOfCurrentMonth = calendar.get(Calendar.MONTH);
                String nameOfCurrentMonth = new DateFormatSymbols().getMonths() [numOfCurrentMonth];
                homeHeader.setText(nameOfCurrentMonth + " " + calendar.get(Calendar.YEAR));
            }
        };

        mAgendaCalendarView = view.findViewById(R.id.agenda_calendar_view);

        mAssignmentsListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot assignmentSnapshot) {
                if (assignmentSnapshot.exists()) {
                    nothingToSeeNowMessage.setVisibility(View.GONE);

                    for (DataSnapshot childSnapshot : assignmentSnapshot.getChildren()) {
                        String userID = childSnapshot.child("userID").getValue().toString();

                        mMyProfessorsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot professorSnapshot) {
                                if (professorSnapshot.hasChild(userID) && index < assignmentSnapshot.getChildrenCount()) {
                                    extractInformationFromFirebase(childSnapshot, eventList);
                                    if (index < eventList.size() && index >= 0) {
                                        eventList.get(index).setId(mID);
                                        index++;
                                        mID++;
                                    }
                                }

                                mAgendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), mPickerController);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    progressBar.setVisibility(View.GONE);
                }

                else
                    nothingToSeeNowMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(buttonView -> {
            Intent addEventIntent = new Intent(getActivity(), AddAssignmentActivity.class);
            addEventIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(addEventIntent);
        });
    }

    private void showOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);

        for (int index = 0; index < fullNamesList.size(); index++) {
            String fullName = fullNamesList.get(index);
            popupMenu.getMenu().add(1, index, index, fullName);
        }

        for (int i = 0; i < fullNamesList.size(); i++) {
            MenuItem menuItem = popupMenu.getMenu().getItem(i);

            mFullNameToIDReference.child(fullNamesList.get(menuItem.getItemId())).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userID = snapshot.child("userID").getValue().toString();
                    menuItem.setContentDescription(userID);

                    mMyProfessorsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String professorUserID = menuItem.getContentDescription().toString();
                            if (snapshot.hasChild(professorUserID))
                                menuItem.setChecked(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.isChecked()) {
                mMyProfessorsReference.child(currentUserID).child(menuItem.getContentDescription().toString()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String professorName = fullNamesList.get(menuItem.getItemId());
                        Toast.makeText(getActivity(), professorName + " was sucessfully removed from the professors you wish to see in your calendar!", Toast.LENGTH_LONG).show();
                    }
                });

                mMyStudentsReference.child(menuItem.getContentDescription().toString()).child(currentUserID).removeValue();

                index--;
                mID--;

                menuItem.setChecked(true);
            }

            else {
                HashMap professorMap = new HashMap();
                    professorMap.put("userID", menuItem.getContentDescription().toString());
                    professorMap.put("fullName", fullNamesList.get(menuItem.getItemId()));

                mMyProfessorsReference.child(currentUserID).child(menuItem.getContentDescription().toString()).updateChildren(professorMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String professorName = fullNamesList.get(menuItem.getItemId());
                        Toast.makeText(getActivity(), professorName + " was sucessfully added to the professors you wish to see in your calendar!", Toast.LENGTH_LONG).show();
                    }
                });

                HashMap studentMap = new HashMap();
                    studentMap.put("userID", currentUserID);
                    studentMap.put("fullName", currentUserFullName);

                mMyStudentsReference.child(menuItem.getContentDescription().toString()).child(currentUserID).updateChildren(studentMap);

                index++;
                mID++;

                menuItem.setChecked(false);
            }

            menuItem.setChecked(! menuItem.isChecked());

            refreshHomeFragment();

            return false;
        });

        popupMenu.getMenu().setGroupCheckable(1, true, false);
        popupMenu.show();
    }

    private void refreshHomeFragment() {
        Intent homeIntent = new Intent(getActivity(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private void sendUserToDetailsActivity(CalendarEvent calendarEvent) {
        Intent detailsIntent = new Intent(getActivity(), DetailsActivity.class);
        detailsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        detailsIntent.putExtra("assignmentUniqueID", assignmentIDList.get((int) calendarEvent.getId()));
        startActivity(detailsIntent);
    }

    private void extractInformationFromFirebase(DataSnapshot childSnapshot, List<CalendarEvent> eventList) {
        String postedDate = childSnapshot.child("postedDate").getValue().toString();
        String dueDate = childSnapshot.child("dueDate").getValue().toString();
        String postedTime = childSnapshot.child("postedTime").getValue().toString();
        String dueTime = childSnapshot.child("dueTime").getValue().toString();
        String title = childSnapshot.child("assignmentTitle").getValue().toString();
        String description = childSnapshot.child("assignmentDescription").getValue().toString();
        String fullName = childSnapshot.child("fullName").getValue().toString();
        String assignmentID = childSnapshot.child("assignmentID").getValue().toString();

        assignmentIDList.add(assignmentID);



        Calendar postedDateAndTime = Calendar.getInstance();
        SimpleDateFormat sdfPostedDateAndTime = new SimpleDateFormat("MMMM dd, yyyy h:mm aa");

        try {
            postedDateAndTime.setTime(sdfPostedDateAndTime.parse(postedDate + " " + postedTime));
        }

        catch (ParseException e) {
            e.printStackTrace();
        }



        Calendar dueDateAndTime = Calendar.getInstance();
        SimpleDateFormat sdfDueDateAndTime = new SimpleDateFormat("MMMM dd, yyyy h:mm aa");

        try {
            dueDateAndTime.setTime(sdfDueDateAndTime.parse(dueDate + " " + dueTime));
        }

        catch (ParseException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        if (! hashMap.containsKey(fullName))
            hashMap.put(fullName, Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256)));

        if (getActivity() != null) {
            BaseCalendarEvent event = new BaseCalendarEvent(title, description, fullName,
                    hashMap.get(fullName), dueDateAndTime, dueDateAndTime, true);
            eventList.add(event);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("personType")) {
                    String personType = snapshot.child("personType").getValue().toString();
                    if (personType.equals("Student"))
                        addEventButton.setVisibility(View.GONE);
                    else
                        optionsButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}