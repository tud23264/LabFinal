package com.example.colin.labfinal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    String currentlyViewedStock;

    TextView name;
    TextView price;
    TextView percent;
    TextView opening;
    TextView volume;
    ListView listView;
    Spinner spin;

    public class NewsFeed {
        public String newsTitle;
        public String newsURL;
    }
    private onClickButtonListener mListener;
    public interface onClickButtonListener {
        public void onAddStock(String symbol);
    }

    ArrayList<NewsFeed> newsList = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timeCode = "1d";
                if (position == 0) timeCode = "1d";
                if (position == 1) timeCode = "5d";
                if (position == 2) timeCode = "1m";
                if (position == 3) timeCode = "6m";
                if (position == 4) timeCode = "1y";
                new getImage().execute("https://chart.yahoo.com/z?t=" + timeCode+"&s=" + currentlyViewedStock);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Bundle args = getArguments();
        if ((args != null)) {
            String symbol = args.getString("key");
            Log.d("detailsReceivedSymbol", symbol);
            currentlyViewedStock = symbol;
            new CallStockDataAPI().execute(symbol);
            new getImage().execute("https://chart.yahoo.com/z?t=1d&s=" + symbol);
            new ParseXML().execute(symbol);
        }
    }

    private class CallStockDataAPI extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String[] info = null;
            try {
                String url = "http://finance.yahoo.com/webservice/v1/symbols/" + params[0] +"/quote?format=json&view=basic" ;



                BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                String response = "", temp = "";
                while ((temp = reader.readLine()) != null) {
                    response += temp;
                }
                try {


                    JSONObject responseObject = new JSONObject( response ); //NOTE: to fix error, do a substring search and then insert a : into the string after ssCallback

                    JSONObject tempValues = responseObject.getJSONObject("list");
                    JSONArray tempValues2 = tempValues.getJSONArray("resources");
                    JSONObject tempValues3 = tempValues2.getJSONObject(0);
                    JSONObject values;

                    JSONObject tempValues4 = tempValues3.getJSONObject("resource");
                    values = tempValues4.getJSONObject("fields");
                    Log.d("jsonarray tester", values.toString());
                    info = new String[5];
                    int i = 0;
                    info[i++] = values.getString("name");
                    info[i++] = values.getString("price");
                    info[i++] = values.getString("chg_percent");
                    info[i++] = String.valueOf(values.getDouble("price") - values.getDouble("change"));
                    info[i] = values.getString("volume");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }

        protected void onPostExecute(String[] info) {
            try{

                String nameLabel = getString(R.string.text_name) + info[0];
                String priceLabel = getString(R.string.text_price )+ info[1];
                String percentLabel = getString(R.string.text_percent) + info[2];
                String openLabel =getString( R.string.text_oprice) + info[3];
                String volumeLabel = getString(R.string.text_volume) + info[4];
                name.setText(nameLabel);
                price.setText(priceLabel);
                percent.setText(percentLabel);
                opening.setText(openLabel);
                volume.setText(volumeLabel);
                for (int i = 0; i < info.length; i++)
                    Log.d("onPost Test", info[i].toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private class getImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            //String[] suggestions = null;
            Bitmap bitmap = null;
            InputStream in = null;
            BufferedOutputStream out = null;


                String url = params[0];


                try {
                    in = new BufferedInputStream(new URL(url).openStream(), 4096);

                    final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                    out = new BufferedOutputStream(dataStream, 4096);
                    copy(in, out);
                    out.flush();

                    final byte[] data = dataStream.toByteArray();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 1;

                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                } catch (IOException e) {
                    Log.e("imageLoadError", "Could not load Bitmap from: " + url);
                } finally {
                    closeStream(in);
                    closeStream(out);
                }

                return bitmap;
        }

        protected void onPostExecute(Bitmap bmp) {

            img.setImageBitmap(bmp);

        }
    }
    public static DetailsFragment newInstance(String param1, String param2) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e("closeStreamError", "Could not close stream", e);
            }
        }
    }


    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
    // ******************************** ENTERING XML PARSING TERRITORY *********************************


    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;
        String title=null;
        String link=null;
        int counter = 0;
        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                    String name=myParser.getName();

                    switch (event){
                        case XmlPullParser.START_TAG:
                            break;

                        case XmlPullParser.TEXT:
                            text = myParser.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if(name.equals("title")){
                                title = text;
                            }


                            else if(name.equals("link")){
                                if (text.contains("*")) {
                                    link = text.substring(text.indexOf("*")+ 1);//myParser.getAttributeValue(null,"value");
                                    NewsFeed tempNews = new NewsFeed();
                                    tempNews.newsTitle = title;
                                    tempNews.newsURL = link;
                                    newsList.add(tempNews);

                                    for (int i = 0; i < newsList.size(); i++) Log.d("newsFeedArrayTest", newsList.get(i).newsURL.toString() + ", " +newsList.get(i).newsTitle.toString());
                                }

                            }

                            else{

                                //Log.d("XMLTESTZONE", title + ", at " + link); //12/7 TO DO: fix this thing. links are coming in but they need to be formatted properly.
                            }
                            break;
                    }
                event = myParser.next();
            }
            String[] newsTitlesArray = new String[newsList.size()];
            for (int i = 0; i < newsList.size(); i++) newsTitlesArray[i] = newsList.get(i).newsTitle;
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, newsTitlesArray);
            setListViewAdapter(stringArrayAdapter);
            parsingComplete = false;

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListViewAdapter(ArrayAdapter<String> adapter) {
        listView.setAdapter(adapter);
    }

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;
    private class ParseXML extends AsyncTask<String, Void, XmlPullParser> {
        @Override
        protected XmlPullParser doInBackground(String... params) {
            String[] info = null;
            XmlPullParser myparser = null;
            try {
                URL url = new URL("http://finance.yahoo.com/rss/2.0/headline?s=" + params[0] +"&region=US&lang=en-US");




                try {

                    //URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream stream = conn.getInputStream();
                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (Exception e){
                e.printStackTrace();
            }
            return myparser;
        }

        protected void onPostExecute(XmlPullParser info) {
            try{

                parseXMLAndStoreIt(info);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    // ******************************** EXITING XML PARSING TERRITORY *********************************



    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    ImageView img;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v =inflater.inflate(R.layout.fragment_details, container, false);
        img = (ImageView)v.findViewById(R.id.imgChart);

        name = (TextView)v.findViewById(R.id.txtName);
        price = (TextView)v.findViewById(R.id.txtPrice);
        percent = (TextView)v.findViewById(R.id.txtChange);
        opening = (TextView)v.findViewById(R.id.txtOpening);
        volume = (TextView)v.findViewById(R.id.txtVolume);
        listView = (ListView)v.findViewById(R.id.lstNews);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri browserUri = Uri.parse(newsList.get(position).newsURL);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(launchBrowser);
            }
        });

        spin = (Spinner)v.findViewById(R.id.spnTime);
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.spinner_text));
        spin.setAdapter(stringArrayAdapter);

        final Button btnURL = (Button)(v.findViewById(R.id.btnAddStock));
        btnURL.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("currentlyViewedStock", currentlyViewedStock);

                mListener.onAddStock(currentlyViewedStock);


            }
        });

        return v;
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






    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
