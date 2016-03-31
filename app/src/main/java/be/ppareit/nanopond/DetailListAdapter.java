package be.ppareit.nanopond;


import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class DetailListAdapter extends BaseAdapter {

    Activity mActivity;
    NanoPondView mView;
    NanoPond mNanopond;

    NanoPond.Cell mActiveCell = null;
    int mActiveX = -1;
    int mActiveY = -1;

    public DetailListAdapter(Context context, NanoPondView view, NanoPond np) {
        mActivity = (Activity) context;
        mNanopond = np;
        mView = view;

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                while (true) {
                    mActivity.runOnUiThread(() -> {
                        if (mView.isCellActive()) {
                            mActiveX = mView.getActiveCellCol();
                            mActiveY = mView.getActiveCellRow();
                            mActiveCell = mNanopond.pond[mActiveX][mActiveY];
                        } else {
                            mActiveCell = null;
                        }
                        notifyDataSetChanged();
                    });
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        if (mActiveCell == null)
            return null;
        switch (position) {
        case 0:
            return new Pair<String, String>("Lineage", String.valueOf(mActiveCell.lineage));
        case 1:
            return new Pair<String, String>("Energy", String.valueOf(mActiveCell.energy));
        case 2:
            return new Pair<String, String>("X", String.valueOf(mActiveX));
        case 3:
            return new Pair<String, String>("Y", String.valueOf(mActiveY));
        case 4:
            return new Pair<String, String>("ID", String.valueOf(mActiveCell.ID));
        case 5:
            return new Pair<String, String>("ParentID", String.valueOf(mActiveCell.parentID));
        case 6:
            return new Pair<String, String>("Generation", String.valueOf(mActiveCell.generation));
        case 7:
            return new Pair<String, String>("Hexa", hexa(mActiveCell.genome));
        case 8:
            return new Pair<String, String>("Disassemble", disassemble(mActiveCell.genome, mNanopond));
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
            LayoutInflater li = mActivity.getLayoutInflater();
            v = li.inflate(R.layout.report_entry, null);
        }
        TextView keyView = (TextView) v.findViewById(R.id.entry_key);
        TextView valueView = (TextView) v.findViewById(R.id.entry_value);
        Pair<String, String> item = getItem(position);
        if (item != null) {
            keyView.setText(item.first);
            valueView.setText(item.second);
        }
        return v;
    }

    static private String hexa(byte[] genome) {
        StringBuilder out=new StringBuilder();
        for (byte aGenome : genome) {
            out.append(Integer.toHexString(aGenome));
        }
        return out.substring(0,out.indexOf("ff")+1);
    }

    static String disassemble(byte[] genome, NanoPond np) {
        String out = "";
        for (int i = 0; i < genome.length; i++) {
            out += i + "\t" + np.names[genome[i]] + "\n";
        }
        return out;
    }
}



















