package com.chat.omar.simplechat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {
    private Context mContext;
    private String[]  Title;
    private String[] description;

    public ChatAdapter(Context context, String[] text1,String[] description) {
        this.mContext = context;
        this.Title = text1;
        this.description = description;

    }

    @Override
    public int getCount() {
        return Title.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View v;
        assert inflater != null;
        v = inflater.inflate(R.layout.listview_layout,viewGroup,false);
        TextView title,descTitle;
        ImageView image;
        descTitle = v.findViewById(R.id.descTitle);
        image = v.findViewById(R.id.imgIcon);
        title = v.findViewById(R.id.txtTitle);
        title.setText(Title[i]);
        descTitle.setText(description[i]);
        image.setImageResource(R.drawable.chevronp);
        return v;
    }
}
