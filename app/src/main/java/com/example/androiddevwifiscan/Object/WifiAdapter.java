package com.example.androiddevwifiscan.Object;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddevwifiscan.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiViewHolder> {
    private List<OutData> lst;
    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item,parent,false);

        return new WifiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder holder, int position) {

        OutData outData = lst.get(position);
        if (outData == null) {
            return;
        }
        holder.txtSSID.setText(outData.getSSID());
        holder.txtBSSID.setText(outData.getBSSID());
        holder.txtLevel.setText(outData.getLevel() + " dBm");

        // Rào dữ liệu
        String decimalFormat = String.format("#0.%0" + 3 + "d", 0);
        DecimalFormat df = new DecimalFormat(decimalFormat);
        // Ox
        String Gx = df.format(outData.getGx());
        String Gy = df.format(outData.getGy());
        String Gz = df.format(outData.getGz());
        // oy
        String Ax = df.format(outData.getAx());
        String Ay = df.format(outData.getAy());
        String Az = df.format(outData.getAz());
        // Oz
        String Mx = df.format(outData.getMx());
        String My = df.format(outData.getMy());
        String Mz = df.format(outData.getMz());

        if (outData.getGx() == 0.0 || outData.getGy() == 0.0 || outData.getGz() == 0.0) {

            holder.txtGx.setText("(" + Gx);
            holder.txtGy.setText(";  " + Gy);
            holder.txtGz.setText(";  " + Gz + ")");

            holder.txtAx.setText("(" + Ax + " ; ");
            holder.txtAy.setText(Ay + " ; ");
            holder.txtAz.setText(Az + ")");

            holder.txtMx.setText("(" + Mx + " ; ");
            holder.txtMy.setText(My + " ; ");
            holder.txtMz.setText(Mz + ")");
        }

        holder.txtGx.setText("(" + Gx);
        holder.txtGy.setText(";  " + Gy);
        holder.txtGz.setText(";  " + Gz + ")");

        holder.txtAx.setText("(" + Ax + " ; ");
        holder.txtAy.setText(Ay + " ; ");
        holder.txtAz.setText(Az + ")");

        holder.txtMx.setText("(" + Mx + " ; ");
        holder.txtMy.setText(My + " ; ");
        holder.txtMz.setText(Mz + ")");

    }
    @Override
    public int getItemCount() {
        if (lst != null) {
            return lst.size();
        }
        return 0;
    }
    public void setData(List<OutData> myList) {
        this.lst = myList;
        notifyDataSetChanged();
    }

    public class  WifiViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSSID,txtBSSID, txtLevel,txtGx,txtGy,txtGz,
                txtAx, txtAy,txtAz, txtMx,txtMy, txtMz;
        public WifiViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSSID = itemView.findViewById(R.id.txt_ssid);
            txtBSSID = itemView.findViewById(R.id.txt_bssid);
            txtLevel = itemView.findViewById(R.id.txt_level);
            txtGx = itemView.findViewById(R.id.txt_gyrox);
            txtGy = itemView.findViewById(R.id.txt_gyroy);
            txtGz = itemView.findViewById(R.id.txt_gyroz);
            txtAx = itemView.findViewById(R.id.txt_accx);
            txtAy = itemView.findViewById(R.id.txt_accy);
            txtAz = itemView.findViewById(R.id.txt_accz);
            txtMx = itemView.findViewById(R.id.txt_magnetic_x);
            txtMy = itemView.findViewById(R.id.txt_magnetic_y);
            txtMz = itemView.findViewById(R.id.txt_magnetic_z);

        }
    }

}
