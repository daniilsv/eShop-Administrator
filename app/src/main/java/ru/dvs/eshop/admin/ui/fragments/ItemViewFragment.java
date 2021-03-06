package ru.dvs.eshop.admin.ui.fragments;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import ru.dvs.eshop.R;
import ru.dvs.eshop.admin.Core;
import ru.dvs.eshop.admin.data.components.Model;
import ru.dvs.eshop.admin.data.components.eshop.Vendor;
import ru.dvs.eshop.admin.ui.activities.ItemActivity;
import ru.dvs.eshop.admin.ui.views.floatingAction.FloatingActionButton;
import ru.dvs.eshop.admin.utils.Function;

public class ItemViewFragment extends Fragment {
    CollapsingToolbarLayout collapsingToolbar;
    ImageView flexibleImage;
    ViewGroup insertPointView;
    FloatingActionButton editFab;
    View fragment_view;
    Model item;
    Bundle args = null;

    public void fillData() {
        if (args == null)
            args = getArguments();
        insertPointView.removeAllViews();
        String type = args.getString("item_type", "-1");
        int itemId = args.getInt("item_id", -1);
        switch (type) {
            case "vendor":
                item = new Vendor().getItemById(itemId);
                collapsingToolbar.setTitle(((Vendor) item).title);
                flexibleImage.setImageDrawable(((Vendor) item).icons.get("big"));
                item.fillViewForReadItem(insertPointView);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_item_view, container, false);

        Toolbar toolbar = (Toolbar) fragment_view.findViewById(R.id.toolbar);
        ((ItemActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator.ofFloat(fragment_view, "alpha", 1, 0).setDuration(500).start();
                getActivity().onBackPressed();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) fragment_view.findViewById(R.id.collapsing_toolbar);
        flexibleImage = (ImageView) fragment_view.findViewById(R.id.flexible_image);
        insertPointView = (ViewGroup) fragment_view.findViewById(R.id.item_frame);


        editFab = (FloatingActionButton) getActivity().findViewById(R.id.item_edit_button);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEdit(false);
            }
        });

        editFab = (FloatingActionButton) getActivity().findViewById(R.id.item_delete_button);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.deleteFromSite(item.original_id, new Function() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                }, new Function() {
                    @Override
                    public void run() {
                        Core.makeToast("Opps... Something was wrong", false);
                    }
                });
            }
        });


        editFab = (FloatingActionButton) getActivity().findViewById(R.id.item_set_invisible_button);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setFieldOnSite("is_enabled", (!item.is_enabled) ? "1" : "0", new Function() {
                    @Override
                    public void run() {
                        Core.makeToast("Changed visible", false);//TODO: Make this by resources
                        item.is_enabled = !item.is_enabled;
                    }
                }, null);
            }
        });


        if (getArguments().getBoolean("is_adding")) {
            getArguments().putBoolean("is_adding", false);
            startEdit(true);
        }
        return fragment_view;
    }

    public void startEdit(boolean is_adding) {
        ItemEditFragment itemEditFragment = new ItemEditFragment();
        itemEditFragment.setArguments(getArguments());
        itemEditFragment.setIsAdding(is_adding);
        ((ItemActivity) getActivity()).placeFragment(itemEditFragment, true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
        getActivity().findViewById(R.id.item_floating_button).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.item_edit_button).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.item_delete_button).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.item_set_invisible_button).setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(fragment_view, "alpha", 0, 1).
                setDuration(500).
                start();
    }
}
