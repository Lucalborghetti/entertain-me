package ch.heiafr.entertainme.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import ch.heiafr.entertainme.R;
import ch.heiafr.entertainme.beans.Media;
import ch.heiafr.entertainme.media.MediaListManager;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private MediaListManager mediaListManager;
    private static Random r = new Random();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView poster;
        public TextView title;
        public TextView date;
        public TextView vote;
        public TextView genre;

        public ViewHolder(View v) {
            super(v);
            poster = (ImageView) v.findViewById(R.id.poster);
            title = (TextView) v.findViewById(R.id.title);
            date = (TextView) v.findViewById(R.id.release_date);
            vote = (TextView) v.findViewById(R.id.vote_average);
            genre = (TextView) v.findViewById(R.id.genre);
        }
    }

    public RecyclerViewAdapter(MediaListManager mlm) {
        this.mediaListManager = mlm;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_cell_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media m = mediaListManager.getMediasList().get(position);
        holder.title.setText(m.getTitle());
        holder.date.setText(m.getDate());
        holder.vote.setText("" + m.getVote());
        holder.genre.setText(m.getGenre());
        if (!m.getPoster().isEmpty()) {
            final Integer tag = r.nextInt();
            holder.poster.setTag(R.string.tag_imageview, tag);
            this.mediaListManager.getPosterPool(position, holder.poster, tag);
        }
    }

    @Override
    public int getItemCount() {
        return mediaListManager.getNbMovies();
    }

}
