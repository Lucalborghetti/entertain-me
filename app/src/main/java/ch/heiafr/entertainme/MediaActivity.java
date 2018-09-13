package ch.heiafr.entertainme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import ch.heiafr.entertainme.beans.Media;
import ch.heiafr.entertainme.beans.Movie;
import ch.heiafr.entertainme.beans.Serie;
import ch.heiafr.entertainme.database.MediaSQLiteHelper;
import ch.heiafr.entertainme.media.MediaListManager;
import ch.heiafr.entertainme.recycler.ActorRecyclerViewAdapter;

/**
 * Created by Lucas on 02.06.2017.
 */

public class MediaActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, MediaListManager.OnRequestCompletedListener {

    // Zoom image from thumb
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    // Media index
    private boolean removeMedia = false;
    private int mediaID;
    private Media media;
    private String mediaTrailerURL;
    private MediaListManager mediaListManager;
    private MediaSQLiteHelper mediaSQLiteHelper;
    // TextViews
    private ImageButton mediaPoster;
    private TextView txt_mediaTitle;
    private TextView txt_mediaRating;
    private TextView txt_mediaTags;
    private TextView txt_mediaDate;
    private TextView txt_mediaLength;
    private TextView txt_mediaDescription;
    private YouTubePlayerView youTube_media_trailer;
    private Button btn_towatch;

    private static final int RECOVERY_REQUEST = 1;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ActorRecyclerViewAdapter mAdapter;

    private Movie movie;
    private Serie serie;
    private int media_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_activity_layout);
        mediaID = getIntent().getExtras().getInt("MEDIA_ID");
        mediaListManager = new MediaListManager(this);
        media_type = getIntent().getExtras().getInt("MEDIA_TYPE");
        if (media_type == MainActivity.MOVIE) {
            mediaListManager.getMovie(mediaID);
        } else {
            mediaListManager.getSerie(mediaID);
        }
        // Hook up clicks on the thumbnail views.
        mediaPoster = (ImageButton) findViewById(R.id.media_poster);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        // Get TextViews
        txt_mediaTitle = (TextView) findViewById(R.id.media_title);
        txt_mediaRating = (TextView) findViewById(R.id.label_media_rating);
        txt_mediaTags = (TextView) findViewById(R.id.label_media_tags);
        txt_mediaDate = (TextView) findViewById(R.id.label_media_date);
        txt_mediaLength = (TextView) findViewById(R.id.label_media_length);
        txt_mediaDescription = (TextView) findViewById(R.id.media_description);
        // Get YouTube Player
        youTube_media_trailer = (YouTubePlayerView) findViewById(R.id.media_trailer);
        // Button to add/remove watch
        btn_towatch = (Button) findViewById(R.id.btn_towatch);
        // Initialise the helper
        mediaSQLiteHelper = new MediaSQLiteHelper(this, MediaSQLiteHelper.DATABASE_NAME, null, MediaSQLiteHelper.DATABASE_VERSION);
    }

    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        Picasso.with(this).load("https://image.tmdb.org/t/p/w500" + media.getPoster()).placeholder(expandedImageView.getDrawable()).into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.cueVideo(mediaTrailerURL);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), youTubeInitializationResult.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTube_media_trailer.initialize(Integer.toString(R.string.YOUTUBE_API_KEY), this);
        }
    }

    @Override
    public void onRequestCompleted() {
        init_media_informations();
    }

    private void init_media_informations() {
        if (media_type == MainActivity.MOVIE) {
            movie = mediaListManager.getMovie();
            media = movie;
        } else {
            serie = mediaListManager.getSerie();
            media = serie;
        }
        if (mediaSQLiteHelper.isMediaInDB(mediaID)) {
            removeMedia = true;
            btn_towatch.setBackgroundResource(R.drawable.ic_watch_remove);
        } else {
            removeMedia = false;
            btn_towatch.setBackgroundResource(R.drawable.ic_watch_add);
        }
        // Media title
        txt_mediaTitle.setText("" + media.getTitle());
        // Media poster
        Picasso.with(this).load("https://image.tmdb.org/t/p/w500" + media.getPoster()).placeholder(mediaPoster.getDrawable()).into(mediaPoster);
        mediaPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageFromThumb(mediaPoster);
            }
        });
        // Media rating
        txt_mediaRating.setText("Note : " + media.getVote());
        // Media tags
        txt_mediaTags.setText(media.getGenre());
        // Media date
        txt_mediaDate.setText(media.getDate());
        // Media specific infos
        if (media_type == MainActivity.MOVIE) {
            txt_mediaLength.setText(movie.getLength() + " minutes");
        } else {
            txt_mediaLength.setText(serie.getNb_episodes() + " épisodes\n" + serie.getNb_seasons() + " saisons");
        }
        // Media description
        txt_mediaDescription.setText(media.getOverview());
        // Media stars
        initActors();
        // Media trailer
        mediaTrailerURL = media.getTrailer();
        youTube_media_trailer.initialize(Integer.toString(R.string.YOUTUBE_API_KEY), this);
    }

    private void initActors() {
        mAdapter = new ActorRecyclerViewAdapter(media.getActors(), this.getApplicationContext());
        mRecyclerView = (RecyclerView) findViewById(R.id.actors_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void mediaTowatch(View view) {
        if (!removeMedia) {
            mediaSQLiteHelper.addNewMedia(mediaID, media_type);
            btn_towatch.setBackgroundResource(R.drawable.ic_watch_remove);
        } else {
            mediaSQLiteHelper.deleteMedia(mediaID, media_type);
            btn_towatch.setBackgroundResource(R.drawable.ic_watch_add);
        }
        removeMedia = !removeMedia;
    }

    private void displayMessageToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
