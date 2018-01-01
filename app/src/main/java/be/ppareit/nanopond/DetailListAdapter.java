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

public class DetailListAdapter extends BaseAdapter {

    private Activity mActivity;
    private NanoPondView mView;
    private NanoPond mNanopond;

    private Cell mActiveCell = null;
    private int mActiveX = -1;
    private int mActiveY = -1;

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
            return new Pair<>("Lineage", String.valueOf(mActiveCell.lineage));
        case 1:
            return new Pair<>("Energy", String.valueOf(mActiveCell.energy));
        case 2:
            return new Pair<>("X", String.valueOf(mActiveX));
        case 3:
            return new Pair<>("Y", String.valueOf(mActiveY));
        case 4:
            return new Pair<>("ID", String.valueOf(mActiveCell.ID));
        case 5:
            return new Pair<>("ParentID", String.valueOf(mActiveCell.parentID));
        case 6:
            return new Pair<>("Generation", String.valueOf(mActiveCell.generation));
        case 7:
            return new Pair<>("Hexa", hexa(mActiveCell.genome));
        case 8:
            return new Pair<>("Disassemble", disassemble(mActiveCell.genome, mNanopond));
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
        TextView keyView = v.findViewById(R.id.entry_key);
        TextView valueView = v.findViewById(R.id.entry_value);
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

    static private String disassemble(byte[] genome, NanoPond np) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < genome.length; i++) {
            out.append(i).append("\t").append(np.names[genome[i]]).append("\n");
        }
        return out.toString();
    }
}



















