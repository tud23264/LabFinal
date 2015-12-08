package com.example.colin.labfinal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

//TO DO: WORK ON THE BUTTON, HAVE IT DISPLAY INFO ABOUT THE SELECtED sTOCK ON DETAILS FRAGMENT
//UPDATE Log 12/6: above still not working. need to be able to parse the JSON object correctly on details fragment.

public class Portfolio extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private onClickButtonListener mListener;
    public interface onClickButtonListener {
        public void onStockView(String symbol);
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Portfolio.
     */
    // TODO: Rename and change types and number of parameters
    public static Portfolio newInstance(String param1, String param2) {
        Portfolio fragment = new Portfolio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Portfolio() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    AutoCompleteTextView auto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);
        final TextView text = (TextView)(v.findViewById(R.id.txtSymbol));





        auto = (AutoCompleteTextView) v.findViewById(R.id.txtSymbol);

        auto.addTextChangedListener(new TextWatcher() {

            // We keep previous length to ensure that we only query for suggestions when the user enters new characters
            int previousTextLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                previousTextLength = charSequence.length();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.d("textChange", Integer.toString(charSequence.length()));
                if (auto.getText().toString().contains(":")) //this should only be called when the user has selected a symbol from the drop-down list
                    auto.setText(auto.getText().toString().substring(0, auto.getText().toString().indexOf(":") ));
                if (charSequence.length() >= 2 )//&& charSequence.length() >= previousTextLength)
                    updateSuggestions(charSequence.toString());


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final Button btnURL = (Button)(v.findViewById(R.id.btnAdd));
        btnURL.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("button check", auto.getText().toString());

                mListener.onStockView(auto.getText().toString());


            }
        });
        return v;
    }


    public void updateSuggestions(String substring){
        new CallSuggestionsAPI().execute(substring);
    }

//  Updates the adapter with suggestions based on the last typed characters
private class CallSuggestionsAPI extends AsyncTask<String, Void, String[]> {
    @Override
    protected String[] doInBackground(String... params) {
        String[] suggestions = null;
        try {
            String url = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + params[0] +"&region=US&lang=en-US&row=ALL&callback=YAHOO.Finance.SymbolSuggest.ssCallback" ;



            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String response = "", temp = "";
            while ((temp = reader.readLine()) != null) {
                response += temp;
            }
            try {

                response = response.replace("YAHOO.Finance.SymbolSuggest.ssCallback(", "");
                response = response.replace(");", "");
                JSONObject responseObject = new JSONObject( response ); //NOTE: to fix error, do a substring search and then insert a : into the string after ssCallback

                JSONObject tempValues = responseObject.getJSONObject("ResultSet");
                JSONArray values = tempValues.getJSONArray("Result");
                Log.d("jsonarray tester", values.toString());
                suggestions = new String[values.length()];
                for (int i = 0; i < values.length(); i++)
                    suggestions[i] = values.getJSONObject(i).getString("symbol") + ": " + values.getJSONObject(i).getString("name");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return suggestions;
    }

    protected void onPostExecute(String[] suggestions) {
        try{
            auto.setAdapter(new ArrayAdapter<>(getActivity().getBaseContext(), android.R.layout.simple_dropdown_item_1line, suggestions));
            auto.setThreshold(2);
            for (int i = 0; i < suggestions.length; i++)
                Log.d("onPost Test", suggestions[i].toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

    // Method to start the service
    public void startService(View view, String url) {
        Intent mServiceIntent = new Intent(getActivity().getApplicationContext(), StockService.class);
        mServiceIntent.putExtra("url", url);

        getActivity().startService(mServiceIntent);
    }

    // Method to stop the service
    public void stopService(View view) {
        getActivity().stopService(new Intent(getActivity().getBaseContext(), StockService.class));
    }
    // TODO: Rename method, update argument and hook method into UI event



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (onClickButtonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ " must implement listener.");
        }

    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}