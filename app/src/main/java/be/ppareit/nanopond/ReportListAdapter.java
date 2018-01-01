/*******************************************************************************
 * Copyright (c) 2011 - 2018 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/

package be.ppareit.nanopond;


import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ReportListAdapter extends BaseAdapter {

    private Activity activity;
    private NanoPond nanopond;
    private volatile NanoPond.Report report;

    public ReportListAdapter(Context context, NanoPond np) {
        activity = (Activity) context;
        nanopond = np;
        report = nanopond.getReport();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                while (true) {
                    report = nanopond.getReport();
                    activity.runOnUiThread(() -> notifyDataSetChanged());
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Pair<String, Long> getItem(int position) {
        switch (position) {
            case 0:
                return new Pair<>("Year", report.year);
            case 1:
                return new Pair<>("Energy", report.energy);
            case 2:
                return new Pair<>("Max generation", report.maxGeneration);
            case 3:
                return new Pair<>("Active cells", report.activeCells);
            case 4:
                return new Pair<>("Viable replicators", report.viableReplicators);
            case 5:
                return new Pair<>("Kills", report.kills);
            case 6:
                return new Pair<>("Replaced", report.replaced);
            case 7:
                return new Pair<>("Shares", report.shares);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater li = activity.getLayoutInflater();
            v = li.inflate(R.layout.report_entry, null);
        }
        TextView keyView = v.findViewById(R.id.entry_key);
        TextView valueView = v.findViewById(R.id.entry_value);
        Pair<String, Long> item = getItem(position);
        keyView.setText(item.first);
        valueView.setText(item.second.toString());
        return v;
    }

}
