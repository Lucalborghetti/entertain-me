package ch.heiafr.entertainme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import ch.heiafr.entertainme.database.MediaSQLiteHelper;
import ch.heiafr.entertainme.media.MediaListManager;
import ch.heiafr.entertainme.recycler.RecyclerItemClickListener;
import ch.heiafr.entertainme.recycler.RecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MediaListManager.OnRequestCompletedListener {

    public static final int MOVIE = 1;
    public static final int SERIE = 2;
    private int media_type;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private MediaListManager mediaListManager;
    private MediaSQLiteHelper mediaSQLiteHelper;
    private ProgressBar pb;
    private Toolbar toolBar;

    private static final int MEDIA_ID_RESULT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialise the helper
        mediaSQLiteHelper = new MediaSQLiteHelper(this,
                MediaSQLiteHelper.DATABASE_NAME, null,
                MediaSQLiteHelper.DATABASE_VERSION);
        init_movies();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        pb.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_movie_popular) {
            toolBar.setTitle(R.string.popular_movies);
            media_type = MOVIE;
            mediaListManager.updateMediasList(MediaListManager.POPULAR_URL, MediaListManager.MOVIE_URL);
        } else if (id == R.id.nav_movie_nowplaying) {
            toolBar.setTitle(R.string.now_playing_movies);
            media_type = MOVIE;
            mediaListManager.updateMediasList(MediaListManager.NOWPLAYING_URL, MediaListManager.MOVIE_URL);
        } else if (id == R.id.nav_movie_upcoming) {
            toolBar.setTitle(R.string.upcoming_movies);
            media_type = MOVIE;
            mediaListManager.updateMediasList(MediaListManager.UPCOMING_URL, MediaListManager.MOVIE_URL);
        } else if (id == R.id.nav_serie_popular) {
            toolBar.setTitle(R.string.popular_series);
            media_type = SERIE;
            mediaListManager.updateMediasList(MediaListManager.POPULAR_URL, MediaListManager.SERIE_URL);
        } else if (id == R.id.nav_serie_ontheair) {
            toolBar.setTitle(R.string.ontheair_movie);
            media_type = SERIE;
            mediaListManager.updateMediasList(MediaListManager.ONTHEAIR_URL, MediaListManager.SERIE_URL);
        } else if (id == R.id.nav_serie_airingtoday) {
            toolBar.setTitle(R.string.airingtoday_movie);
            media_type = SERIE;
            mediaListManager.updateMediasList(MediaListManager.AIRINGTODAY, MediaListManager.SERIE_URL);
        } else if (id == R.id.nav_movie_towatch) {
            toolBar.setTitle(R.string.towatch_movies);
            media_type = MOVIE;
            mediaListManager.updateMediasList(mediaSQLiteHelper.getMediasFromDB(MOVIE), MediaListManager.MOVIE_URL);
        } else if (id == R.id.nav_serie_towatch) {
            toolBar.setTitle(R.string.towatch_series);
            media_type = SERIE;
            mediaListManager.updateMediasList(mediaSQLiteHelper.getMediasFromDB(SERIE), MediaListManager.SERIE_URL);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Initialize the phone music grid.
     */
    public void init_movies() {
        media_type = MOVIE;
        mediaListManager = new MediaListManager(this);
        mediaListManager.updateMediasList(MediaListManager.POPULAR_URL, MediaListManager.MOVIE_URL);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    /**
     * This method is called when an movie on the list has been clicked.
     */
    private RecyclerItemClickListener.OnItemClickListener movieListListener = new RecyclerItemClickListener.OnItemClickListener() {
        public void onItemClick(View v, int position) {
            int mediaID = mediaListManager.getMediasList().get(position).getId();
            Intent mediaIntent = new Intent(getApplicationContext(), MediaActivity.class);
            mediaIntent.putExtra("MEDIA_ID", mediaID);
            mediaIntent.putExtra("MEDIA_TYPE", media_type);
            setResult(MEDIA_ID_RESULT, mediaIntent);
            startActivity(mediaIntent);
        }
    };

    @Override
    public void onRequestCompleted() {
        pb.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (mAdapter == null) {
            mAdapter = new RecyclerViewAdapter(mediaListManager);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, movieListListener));
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
        return true;
    }

    private void displayMessageToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
