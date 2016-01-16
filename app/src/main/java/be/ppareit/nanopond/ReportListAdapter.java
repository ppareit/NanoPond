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

    Activity activity;
    NanoPond nanopond;
    volatile NanoPond.Report report;

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
            return new Pair<String, Long>("Year", report.year);
        case 1:
            return new Pair<String, Long>("Energy", report.energy);
        case 2:
            return new Pair<String, Long>("Max generation", report.maxGeneration);
        case 3:
            return new Pair<String, Long>("Active cells", report.activeCells);
        case 4:
            return new Pair<String, Long>("Viable replicators", report.viableReplicators);
        case 5:
            return new Pair<String, Long>("Kills", report.kills);
        case 6:
            return new Pair<String, Long>("Replaced", report.replaced);
        case 7:
            return new Pair<String, Long>("Shares", report.shares);
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
        TextView keyView = (TextView) v.findViewById(R.id.entry_key);
        TextView valueView = (TextView) v.findViewById(R.id.entry_value);
        Pair<String, Long> item = getItem(position);
        keyView.setText(item.first);
        valueView.setText(item.second.toString());
        return v;
    }

}
