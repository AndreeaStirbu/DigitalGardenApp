package com.example.digitalgarden.Fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.Model.Plant;
import com.example.digitalgarden.Adapters.PlantsAdapter;
import com.example.digitalgarden.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Fragment that displays the list of plants.
 */
public class PlantsViewFragment extends Fragment implements PlantsAdapter.OnPlantListener {
    private ArrayList<Plant> mPlantsList;
    private DashboardActivity mActivity;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecycle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.plants_view_fragment, container, false);

        // Get the parent mActivity
        mActivity = (DashboardActivity) getActivity();

        // Initialise the Recycler View
        mRecycle = rootView.findViewById(R.id.recyclerViewPlants);
        setRecycleView();

        // Add a plant
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.plants_display, new AddPlantFragment()).addToBackStack(null);
                transaction.commit();
            }
        });

        // Refresh
        mSwipeRefresh = rootView.findViewById(R.id.refreshPlantsDashboard);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(false);
                setRecycleView();
            }
        });

        return rootView;
    }


    /**
     * Initialise the Recycler View and display the list of plants stored in Firebase
     **/
    public void setRecycleView(){
        // Initialise the plant of lists with the plants loaded in the main activity
        mPlantsList = mActivity.plantsList;

        // Create the plant adapter
        PlantsAdapter plantsAdapter = new PlantsAdapter(mPlantsList, mActivity, this);

        //Setting the RecyclerView
        mRecycle.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycle.setLayoutManager(mLayoutManager);
        mRecycle.setAdapter(plantsAdapter);
    }

    /**
     * When a plant card is clicked, open the fragment that displays the details of that plant
     * @param position The position of the plant that is being clicked
     */
    @Override
    public void onPlantClick(int position) {
        ViewPlantFragment viewPlantFragment = new ViewPlantFragment();
        viewPlantFragment.plant = mPlantsList.get(position);
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.plants_display, viewPlantFragment).addToBackStack(null);
        transaction.commit();
    }
}
