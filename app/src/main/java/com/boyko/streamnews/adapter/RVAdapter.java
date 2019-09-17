package com.boyko.streamnews.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.boyko.streamnews.MainActivity;
import com.boyko.streamnews.R;
import com.boyko.streamnews.model.ObjectNew;
import com.boyko.streamnews.utils.InternetConnection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int NOINET = 2;

    private String url = null;
    private ArrayList<ObjectNew> object_for_adapter;
    Context context;
    private customButtonListener customListner;

    public RVAdapter(Context context, ArrayList result) {
        this.context = context;
        this.object_for_adapter = result;
    }
    public interface customButtonListener {
        void onButtonClickListner();
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }
    @Override
    public int getItemViewType(int position) {

        if (
                (!InternetConnection.checkConnection(context)
                && position == object_for_adapter.size() - 1
                && MainActivity.current_page!=MainActivity.TOTAL_PAGE)
            )
            return NOINET;
        return (
                position == object_for_adapter.size() - 1
                && MainActivity.current_page!=MainActivity.TOTAL_PAGE
                ) ? LOADING : ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        RecyclerView.ViewHolder holder = null;

        switch (viewType) {
            case ITEM:

                View view = inflater.inflate(R.layout.layout_row_view,viewGroup,false);
                holder = new ViewHolder(view);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, viewGroup, false);
                holder = new LoadingVH(v2);
                break;
            case NOINET:
                View v3 = inflater.inflate(R.layout.item_no_internet, viewGroup, false);
                View v4 = inflater.inflate(R.layout.activity_main, viewGroup, false);
                FloatingActionButton b = v4.findViewById(R.id.fab);
                b.show();
                holder = new NoInetVH(v3);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        ObjectNew ObNew;

        switch (getItemViewType(position)) {
            case ITEM:
                final ViewHolder vh = (ViewHolder) viewHolder;
                vh.iProgress.setVisibility(View.VISIBLE);
                ObNew = object_for_adapter.get(position);
                vh.textDate.setText( ObNew.getPublishedAt().substring(0,10)+"  " + ObNew.getPublishedAt().substring(12,19));
                vh.textDectibe.setText(ObNew.getDescription());
                vh.textName.setText(ObNew.getTitle()+" "+position);
                url = object_for_adapter.get(position).getUrlToImage();
                if (url!=null && url.length()>0)

                Picasso
                        .get()
                        .load(url)
                        .error(R.mipmap.ic_no_image)
                        .into(vh.image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //Success image already loaded into the view
                                vh.iProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                //Error placeholder image already loaded into the view, do further handling of this situation here
                                 vh.iProgress.setVisibility(View.GONE);
                            }
                        });
                else vh.iProgress.setVisibility(View.GONE);

                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (InternetConnection.checkConnection(context)){
                            Intent browserIntent = new
                                    Intent(Intent.ACTION_VIEW, Uri.parse("http://"+object_for_adapter.get(position).getUrl()));
                            context.startActivity(browserIntent);
                        } else {

                            Snackbar.make(v, R.string.string_internet_connection_not_available, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case LOADING:
                break;
            case NOINET:
                NoInetVH vhnI = (NoInetVH) viewHolder;
                vhnI.textNoInet.setText("Нет интернета");
                break;
        }

        //Pre fetch следующих 5 изображений
        prefetch(object_for_adapter, position,5);
    }

    @Override
    public int getItemCount() {
        return object_for_adapter.size();
    }

//______________________________________ Классы для Holders
    //____________ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder{


    private ImageView image;
    private TextView textName;
    private TextView textDectibe;
    private TextView textDate;
    //private RelativeLayout itemLayout;
    private ProgressBar iProgress;

        public ViewHolder(View itemView) {
            super(itemView);


            image =         (ImageView) itemView.findViewById(R.id.imageView);
            textName =      (TextView) itemView.findViewById(R.id.textViewName);
            textDectibe =   (TextView)itemView.findViewById(R.id.textViewDescribe);
            textDate =      (TextView)itemView.findViewById(R.id.textViewDate);
            //itemLayout =    itemView.findViewById(R.id.item_layout);
            iProgress = (ProgressBar) itemView.findViewById(R.id.item_progress);
        }
    }
    //_______________LoadingVH
    public class LoadingVH extends RecyclerView.ViewHolder {

        public LoadingVH(View itemView) {
            super(itemView);
        }
    }
    //_______________NoInetVH
    public class NoInetVH extends RecyclerView.ViewHolder {
        TextView textNoInet;
        Button btn;
        public NoInetVH(View itemView) {
            super(itemView);
            textNoInet = itemView.findViewById(R.id.textNoInternet);
            btn = itemView.findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customListner != null) {
                        customListner.onButtonClickListner();
                    }
                }
            });
        }
    }
        /*
   Helpers
   _________________________________________________________________________________________________
    */

        public void add(ObjectNew r) {
            object_for_adapter.add(r);
            notifyItemInserted(object_for_adapter.size() - 1);
        }

    public void addAll(ArrayList<ObjectNew> artResults) {
        for (ObjectNew result : artResults) {
            add(result);
        }
    }
    public void clear() {
        int size = object_for_adapter.size();
        object_for_adapter.clear();
        notifyItemRangeRemoved(0, size);

    }

    //Pre fetch следующих 5 изображений
    public static void prefetch(ArrayList<ObjectNew> array, int position, int fetch) {
            for (int i = position ; i < fetch + position; i++) {
            if (i < array.size()){
                String url = array.get(i).getUrlToImage();
                if (url!= null && url.length()!=0){
                    Picasso.get().load(url).fetch();
                }
            }
        }
    }
}