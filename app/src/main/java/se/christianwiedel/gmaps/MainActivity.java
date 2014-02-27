package se.christianwiedel.gmaps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements ActionBar.TabListener {


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public static final int MAP_POSITION = 0;
    public static final int LIST_POSITION = 1;

    private ListFragment mListfragment;
    private MyMapFragment mMapfragment;
    private LocationClient mLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListfragment = new ListFragment();
        mMapfragment = new MyMapFragment();

        mLocationClient = new LocationClient(this, mMapfragment, mMapfragment);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        FragmentManager fm = getFragmentManager();
        mSectionsPagerAdapter = new SectionsPagerAdapter(fm);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case MAP_POSITION:
                    return mMapfragment;
                //return map frag
                case LIST_POSITION:
                    return mListfragment;
                //return list fragment
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case MAP_POSITION:
                    // return R.string.mapFragTitle;
                    //return map frag
                case LIST_POSITION:
                    // return mListfragment;
                    //return list fragment
            }
            return null;
        }
    }

    public static class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.list_fragment, container, false);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    class GeofenceData {

        GeofenceData(float radius, float latitude, float longitude) {
            this.radius = radius;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public float radius;
        public float latitude;
        public float longitude;
    }

    public class MyMapFragment extends MapFragment implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener,
            GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,
            LoaderManager.LoaderCallbacks<Cursor> {

        private static final float GEOFENCE_RADIUS = 25.0f;
        private PolygonOptions mPolygonOptions = null;
        private LocationClient mLocationClient;
        private List<Geofence> mGeofences;
        private Map<String, GeofenceData> mGeofenceDatas;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            mGeofences = new LinkedList<Geofence>();
            mGeofenceDatas = new HashMap<String, GeofenceData>();

/*
            mMap = ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
*/
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            GoogleMap googleMap = getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.setOnMapLongClickListener(this);
            googleMap.setOnMapClickListener(this);
            googleMap.setMyLocationEnabled(true);
            return view;
        }

        @Override
        public void onMapLongClick(LatLng latLng) {
            boolean didRemove = false;

            for (Map.Entry<String, GeofenceData> geofenceDataEntry : mGeofenceDatas.entrySet()) {
                String requestId = geofenceDataEntry.getKey();
                GeofenceData geofenceData = geofenceDataEntry.getValue();
                float[] distance = new float[1];
                Location.distanceBetween(geofenceData.latitude, geofenceData.longitude,
                        latLng.latitude, latLng.longitude, distance);

                if (distance[0] <= GEOFENCE_RADIUS) {
                    didRemove = true;
                    Uri geofenceUri = Uri.withAppendedPath(MyGeofenceStore.Contract.GEOFENCES,
                            requestId);
                    getContentResolver().delete(geofenceUri, null, null);
                    getLoaderManager().restartLoader(0, null, this);
                }
            }

            if (!didRemove) {
                String requestId = insertGeofenceInDatabase(latLng, GEOFENCE_RADIUS);
                addAndPaintGeofence(requestId, latLng, GEOFENCE_RADIUS);
            }
        }

        private void addAndPaintGeofence(String requestId, LatLng latLng, float radius) {
            addGeofencesToList(requestId, latLng, radius);
            Intent showGeofenceToast =
                    new Intent(MyReceiver.ACTION_GEOFENCE_TOAST);
            PendingIntent pendingIntent
                    = PendingIntent
                    .getBroadcast(getActivity(), 0, showGeofenceToast, 0);
            mLocationClient.addGeofences(mGeofences, pendingIntent,
                    new LocationClient.OnAddGeofencesResultListener() {
                        @Override
                        public void onAddGeofencesResult(int i, String[] strings) {
                            // TODO Possible error handling...
                            Log.e("GeofenceDemo", "Geofences added!");
                        }
                    });

            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng).radius(radius).fillColor(Color.RED);
            getMap().addCircle(circleOptions);
        }

        private void addGeofencesToList(String requestId, LatLng latLng, float radius) {
            Geofence.Builder builder = new Geofence.Builder();
            Geofence geofence = builder
                    .setRequestId(requestId)
                    .setCircularRegion(latLng.latitude,
                            latLng.longitude, radius)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                            | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setExpirationDuration(1000 * 60 * 5)
                    .build();

            mGeofences.add(geofence);
            mGeofenceDatas.put(requestId, new GeofenceData(radius,
                    (float) latLng.latitude, (float) latLng.longitude));
        }

        private String insertGeofenceInDatabase(LatLng latLng, float radius) {
            ContentValues values = new ContentValues();
            values.put(MyGeofenceStore.Contract.LATITUDE, latLng.latitude);
            values.put(MyGeofenceStore.Contract.LONGITUDE, latLng.longitude);
            values.put(MyGeofenceStore.Contract.RADIUS, radius);
            values.put(MyGeofenceStore.Contract.CREATED, System.currentTimeMillis());
            return getContentResolver()
                    .insert(MyGeofenceStore.Contract.GEOFENCES, values)
                    .getLastPathSegment();
        }


        /*
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.map_fragment, container, false);
}
*/
        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        @Override
        public void onMapClick(LatLng latLng) {

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }


    }

}
