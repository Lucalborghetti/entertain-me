package ch.heiafr.entertainme.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

import ch.heiafr.entertainme.R;
import ch.heiafr.entertainme.beans.Actor;
import ch.heiafr.entertainme.media.MediaListManager;

public class ActorRecyclerViewAdapter extends RecyclerView.Adapter<ActorRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Actor> actors = new ArrayList<>();
    private static Random r = new Random();
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profile;
        public TextView actorName;
        public TextView characterName;

        public ViewHolder(View v) {
            super(v);
            profile = (ImageView) v.findViewById(R.id.profile);
            actorName = (TextView) v.findViewById(R.id.actorName);
            characterName = (TextView) v.findViewById(R.id.characterName);
        }
    }

    public ActorRecyclerViewAdapter(ArrayList<Actor> actors, Context context) {
        this.actors = actors;
        this.context = context;
    }

    @Override
    public ActorRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.actor_cell_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Actor a = actors.get(position);
        holder.actorName.setText(a.getName());
        holder.characterName.setText(a.getCharacter());
        String imageUrl = MediaListManager.IMAGE_BASE_URL + a.getProfile_path();
        Picasso.with(context).load(imageUrl).into(holder.profile);
    }

    @Override
    public int getItemCount() {
        return actors.size();
    }

}
