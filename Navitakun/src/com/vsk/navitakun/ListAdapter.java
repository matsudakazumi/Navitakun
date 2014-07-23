package com.vsk.navitakun;

import java.util.ArrayList;

import com.vsk.navitakun.db.NaviHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater = null;
    private ArrayList<NaviHistory> history = null;

    public ListAdapter(Context context, ArrayList<NaviHistory> _historyList) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        history = _historyList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_at, null);
        }

        NaviHistory item = (NaviHistory) this.getItem(position);

        if (item != null) {
            TextView memo = (TextView) convertView.findViewById(R.id.idSaveMemo);
            TextView date = (TextView) convertView.findViewById(R.id.idSaveDateTime);
            TextView address = (TextView) convertView.findViewById(R.id.idSaveAddress);
            if (15 <= history.get(position).getDestMemo().length()) {
                memo.setText(history.get(position).getDestMemo().substring(0, 15) + "...");
            } else {
                memo.setText(history.get(position).getDestMemo());
            }
            date.setText(history.get(position).getDateTime());
            if (17 <= history.get(position).getDestAddress().length()) {
                address.setText(history.get(position).getDestAddress().substring(0, 17) + "...");
            } else {
                address.setText(history.get(position).getDestAddress());
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Object getItem(int position) {
        return history.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
