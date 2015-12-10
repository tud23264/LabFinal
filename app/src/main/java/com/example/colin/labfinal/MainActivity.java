package com.example.colin.labfinal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.Context;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


//IMPORTANT: http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=go&region=US&lang=en-US&row=ALL&callback=YAHOO.Finance.SymbolSuggest.ssCallback
public class MainActivity extends AppCompatActivity implements Portfolio.onClickButtonListener, DetailsFragment.onClickButtonListener {

    boolean connected;
    AutoCompleteTextView auto;
    boolean twoPanes;
    ArrayList<String> userPortfolio; //contains all user's stocks
    MenuItem del;
    MenuItem can;
    MenuItem con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        twoPanes = (findViewById(R.id.fragment_2) != null);
        userPortfolio = new ArrayList<>();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        del = menu.findItem(R.id.action_delete);
        can = menu.findItem(R.id.action_cancel);
        con = menu.findItem(R.id.action_confirm);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Bundle args = new Bundle();
        //noinspection SimplifiableIfStatement

        if (id == R.id.action_delete) {
            args.putString("action", "deleteMode");
            can.setVisible(true);
            con.setVisible(true);
            del.setVisible(false);
            loadFragment(R.id.fragment_1, new Portfolio(), true, args);
        }
        if (id == R.id.action_cancel) {
            args.putString("action", "cancel");
            can.setVisible(false);
            con.setVisible(false);
            del.setVisible(true);
            loadFragment(R.id.fragment_1, new Portfolio(), true, args);
        }
        if (id == R.id.action_confirm) {
            args.putString("action", "confirm");
            can.setVisible(false);
            con.setVisible(false);
            del.setVisible(true);
            loadFragment(R.id.fragment_1, new Portfolio(), true, args);
        }




        return super.onOptionsItemSelected(item);
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
    public void onAddStock(String symbol) {
        Bundle args = new Bundle();
        if (!twoPanes) {
            userPortfolio.add(symbol);
            savePortfolio(userPortfolio);
            args.putString("key", symbol);
            loadFragment(R.id.fragment_1, new Portfolio(), true, args);

        }
        else {

        }
    }
    public void savePortfolio(ArrayList<String> stocks) {
        try {
            FileOutputStream outputStream = getApplicationContext().openFileOutput("stocks", Context.MODE_PRIVATE);
            //outputStream.write(stocks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
