package com.bug_apk.locoguard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.libraries.places.api.Places.createClient;

public class SourceAutocompleteFragment extends Fragment {

    private ArrayList<String> mLocationPrimaryName = new ArrayList<String>();
    private ArrayList<String> mLocationSecondaryName = new ArrayList<String>();
    private ArrayList<String> mLocationID = new ArrayList<String>();
    private String mUserSourceText = new String();

    private Fragment currentFragment = this;

    public static SourceAutocompleteFragment newInstance(String mUserSourceText) {
        Log.i("SourceAutocompleteFragment","Creating new fragment instance");

        SourceAutocompleteFragment sourceAutocompleteFragment = new SourceAutocompleteFragment();
        Bundle args = new Bundle();
        args.putString("mUserSourceText", mUserSourceText);
        sourceAutocompleteFragment.setArguments(args);

        Log.i("SourceAutocompleteFragment","Created new fragment instance");
        return sourceAutocompleteFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        Log.i("SourceAutocompleteFragment","OnCreateView: initialized");
        if (getArguments() != null) {
            mUserSourceText = getArguments().getString("mUserSourceText");
        }
        Log.i("SourceAutocompleteFragment","OnCreateView: parent id "+parent.getResources().getResourceEntryName(parent.getId()));
        return inflater.inflate(R.layout.fragment_source_autocomplete, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i("SourceAutocompleteFragment","OnViewCreated: initialized");
        Log.i("SourceAutocompleteFragment","Id "+ view.getResources().getResourceEntryName(view.getId()));
        Log.i("SourceAutocompleteFragment","source "+ mUserSourceText);

        ((ImageView)getView().findViewById(R.id.sourceFragmentBackButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                getActivity().getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            }
        });

        if(mUserSourceText!=null) {
            ((EditText)view.findViewById(R.id.sourceEditText)).setText(mUserSourceText);
            ((EditText)view.findViewById(R.id.sourceEditText)).requestFocus();
            ((EditText)view.findViewById(R.id.sourceEditText)).setSelection(((EditText)view.findViewById(R.id.sourceEditText)).getText().length());
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        //Find autocomplete results
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        ((EditText) getView().findViewById(R.id.sourceEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = ((EditText) getView().findViewById(R.id.sourceEditText)).getText().toString();
                Log.i("GuardActivity", query);
                PlacesClient placesClient = createClient(getView().getContext());
                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        // Call either setLocationBias() OR setLocationRestriction().
//                        .setLocationBias(bounds)
//                        .setLocationRestriction(bounds)
//                        .setCountry("au")
//                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(query)
                        .build();

                Task<FindAutocompletePredictionsResponse> task =
                        placesClient.findAutocompletePredictions(request);

                task.addOnSuccessListener((response) -> {
                    mLocationPrimaryName.clear();
                    mLocationSecondaryName.clear();
                    mLocationID.clear();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        Log.i("GuardActivity", "prediction result: " + prediction);
                        // add result to your arraylist
                        Log.i("SourceAutocompleteFragmentActivity",prediction.getPrimaryText(null).toString());
                        mLocationPrimaryName.add(prediction.getPrimaryText(null).toString());
                        mLocationSecondaryName.add(prediction.getSecondaryText(null).toString());
                        mLocationID.add(prediction.getPlaceId());
                    }
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.sourceRecycleView);
                    RecyclerView.Adapter adapter = new AutocompleteRecyclerViewAdapter(mLocationPrimaryName, mLocationSecondaryName, mLocationID, view.getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    recyclerView.setAdapter(adapter);
                });
                task.addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        // places not found exception code
                        Log.i("GuardActivity", "error message %s" + apiException.getMessage());
                    }
                });
                task.addOnCompleteListener((response) -> {
                    Exception e = task.getException();
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        if (!task.isSuccessful()) {
                            // your code
                        }
                    }
                });
            }
        });

        Log.i("SourceAutocompleteFragment","OnViewCreated: created");
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

}
