package com.chat.omar.simplechat;

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
    private int[] imge;

    public ChatAdapter(Context context, String[] text1) {
        mContext = context;
        Title = text1;

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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View v;
        v = inflater.inflate(R.layout.listview_layout,viewGroup,false);
        TextView title;
        ImageView image;
        image = v.findViewById(R.id.imgIcon);
        title = v.findViewById(R.id.txtTitle);
        title.setText(Title[i]);
        image.setImageResource(R.drawable.chevronp);
        return v;
    }
}
