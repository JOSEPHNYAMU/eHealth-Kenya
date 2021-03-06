package co.ehealth.e_health;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class Three extends Fragment {

    public Three() {
        // Required empty public constructor
    }

    private RecyclerView eBlogs;
    private DatabaseReference eDatabase, eUsers, eLikes;
    private View newsView;
    private FirebaseAuth eAuth;
    String userId = null;
    private TextView visibilityText;
    private Boolean eLikeProcess = false;
    private int likesCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        eDatabase = FirebaseDatabase.getInstance().getReference().child("Blogs");
        eUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        eLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        eAuth = FirebaseAuth.getInstance();
        eUsers.keepSynced(true);
        eLikes.keepSynced(true);
        eDatabase.keepSynced(true);
        eUsers.keepSynced(true);
        userId = eAuth.getCurrentUser().getUid();
        newsView = inflater.inflate(R.layout.fragment_three, container, false);

        eBlogs = (RecyclerView) newsView.findViewById(R.id.blogList);
        eBlogs.setHasFixedSize(true);
        eBlogs.setLayoutManager(new LinearLayoutManager(getContext()));

        visibilityText = (TextView) newsView.findViewById(R.id.no_news);

                return newsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<News> options =
                new FirebaseRecyclerOptions.Builder<News>()
                .setQuery(eDatabase, News.class)
                .build();

        FirebaseRecyclerAdapter<News, NewsViewHolder> adapter =
                new FirebaseRecyclerAdapter<News, NewsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NewsViewHolder holder, int position, @NonNull News model) {

                        final String listID = getRef(position).getKey();


                                eUsers.child(userId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                        if(dataSnapshot.child("Role").getValue().toString().equals("Super")) {

                                            holder.deleteButton.setVisibility(View.VISIBLE);
                                            holder.editButton.setVisibility(View.VISIBLE);

                                        } else {


                                            eDatabase.child(listID).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    if(dataSnapshot.child("author").getValue().toString().equals(userId)) {

                                                        holder.deleteButton.setVisibility(View.VISIBLE);
                                                        holder.editButton.setVisibility(View.VISIBLE);

                                                    } else {

                                                        holder.deleteButton.setVisibility(View.GONE);
                                                        holder.editButton.setVisibility(View.GONE);

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                                        }


                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                                holder.deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setCancelable(true);
                                        builder.setTitle("DELETE THIS BLOG?");
                                        builder.setMessage("Are sure you want to DELETE this Blog?");
                                        builder.setPositiveButton("DELETE",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        eLikes.child(listID).removeValue();

                                                        eDatabase.child(listID).removeValue();

                                                        StyleableToast.makeText(getContext(), "News Blog Successfully Deleted", Toast.LENGTH_LONG, R.style.success).show();

                                                    }
                                                });
                                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {



                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();

                                        return false;
                                    }
                                });


                        eDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                if(!dataSnapshot.hasChildren()) {

                                    visibilityText.setVisibility(View.VISIBLE);
                                    eBlogs.setVisibility(View.GONE);

                                } else {

                                    visibilityText.setVisibility(View.GONE);
                                    eBlogs.setVisibility(View.VISIBLE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        eUsers.child(model.getAuthor().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String First = dataSnapshot.child("Firstname").getValue().toString();
                                String Last = dataSnapshot.child("Lastname").getValue().toString();
                                String Name = "Article By: " + First.concat(" " + Last);
                                holder.authorText.setText(Name);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        final String imageName = model.getPicture().toString();
                        Picasso.get().load(imageName).networkPolicy(NetworkPolicy.OFFLINE).into(holder.blogPicture, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(imageName).into(holder.blogPicture);

                            }
                        });
                        holder.newsTitle.setText(model.getTitle().toString().toUpperCase());
                        String body = model.getBody().toString().concat("...");
                        holder.newsBody.setText(body);
                        holder.setLikeButton(listID);




                        eLikes.child(listID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    likesCount = (int) dataSnapshot.getChildrenCount();

                                    if(Integer.toString(likesCount).equals("1")) {

                                        holder.likeCount.setText(Integer.toString(likesCount) + " Like");

                                    } else {

                                        holder.likeCount.setText(Integer.toString(likesCount) + " Likes");

                                    }


                                } else {

                                    holder.likeCount.setText("0 Likes");

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        holder.likeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                eLikeProcess = true;


                                    eLikes.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (eLikeProcess) {

                                                if (dataSnapshot.child(listID).hasChild(userId)) {

                                                    eLikes.child(listID).child(userId).removeValue();
                                                    eLikeProcess = false;

                                                } else {

                                                    eLikes.child(listID).child(userId).setValue(1);
                                                    eLikeProcess = false;

                                                }

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                        });


                        holder.newsTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent singleUserIntent = new Intent(getContext(), Blog.class);
                                singleUserIntent.putExtra("userID", listID);
                                startActivity(singleUserIntent );

                            }
                        });

                        holder.newsBody.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent singleUserIntent = new Intent(getContext(), Blog.class);
                                singleUserIntent.putExtra("userID", listID);
                                startActivity(singleUserIntent );

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup, false);
                        return new NewsViewHolder(view);

                    }
                };

       eBlogs.setAdapter(adapter);
       adapter.startListening();

    }


    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        View eView;
        TextView newsTitle, newsBody, authorText, likeCount;
        ImageView blogPicture, likeButton, deleteButton, editButton;
        DatabaseReference eLikes;
        FirebaseAuth eAuth;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            eView = itemView;

            newsTitle = (TextView) itemView.findViewById(R.id.tit);
            newsBody = (TextView) itemView.findViewById(R.id.bod);
            likeCount = (TextView) itemView.findViewById(R.id.like_count);
            blogPicture = (ImageView) itemView.findViewById(R.id.pic);
            deleteButton = (ImageView) itemView.findViewById(R.id.delete);
            editButton = (ImageView) itemView.findViewById(R.id.edit);
            authorText = (TextView) itemView.findViewById(R.id.aut);
            likeButton = (ImageView) itemView.findViewById(R.id.like);
            eLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
            eLikes.keepSynced(true);
            eAuth = FirebaseAuth.getInstance();

        }

        public void setLikeButton(final String listID) {

            eLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(listID).hasChild(eAuth.getCurrentUser().getUid())) {

                        likeButton.setImageResource(R.drawable.ic_thumb_up_red);

                    } else {

                        likeButton.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
