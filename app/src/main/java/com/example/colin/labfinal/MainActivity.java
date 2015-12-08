package com.example.colin.labfinal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;



//IMPORTANT: http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=go&region=US&lang=en-US&row=ALL&callback=YAHOO.Finance.SymbolSuggest.ssCallback
public class MainActivity extends AppCompatActivity implements Portfolio.onClickButtonListener {

    boolean connected;
    AutoCompleteTextView auto;
    boolean twoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        twoPanes = (findViewById(R.id.fragment_2) != null);

        //  Load navigation fragment by default
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_1, new Portfolio());
        fragmentTransaction.commit();

        /*
         *  Check if details pain is visible in current layout (e.g. large or landscape)
         *  and load fragment if true.
         */
        if (twoPanes){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_2, new DetailsFragment());
            fragmentTransaction.commit();
        }


    }
    public void onStockView(String symbol) {
        Bundle args = new Bundle();
        if (!twoPanes) {
            args.putString("key", symbol);
            loadFragment(R.id.fragment_1, new DetailsFragment(), true, args);
        }
        else {

        }
    }
    private void loadFragment(int paneId, Fragment fragment, boolean placeOnBackstack, Bundle args){
        FragmentManager fm = getFragmentManager();
        fragment.setArguments(args);
        FragmentTransaction ft = fm.beginTransaction()
                .replace(paneId, fragment);
        if (placeOnBackstack)
            ft.addToBackStack(null);
        ft.commit();

        fm.executePendingTransactions();
    }

}
