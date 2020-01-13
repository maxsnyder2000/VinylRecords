package com.example.maxsn.vinylrecords;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListViewAdapter extends BaseAdapter
{
    public class Record
    {
        public File file;
        public Bitmap bitmap;
        public String title;
        public String artist;
        public int length;
        public boolean original;
        public String label;
        public String serial;
        public String dateReleased;
        public String datePurchased;
        public int plays;

        public Record(File file, Bitmap bitmap, String title, String artist, int length, boolean original, String label, String serial, String dateReleased, String datePurchased, int plays)
        {
            this.file = file;
            this.bitmap = bitmap;
            this.title = title;
            this.artist = artist;
            this.length = length;
            this.original = original;
            this.label = label;
            this.serial = serial;
            this.dateReleased = dateReleased;
            this.datePurchased = datePurchased;
            this.plays = plays;
        }

        public int play() { plays += 1; return plays; }
    }

    private Context context;
    private List<Record> records = new ArrayList<>();

    private int[] length_ids  = { R.id.length_1, R.id.length_2, R.id.length_3, R.id.length_4, R.id.length_5 };
    private int[] length_strs = { R.string.length_1, R.string.length_2, R.string.length_3, R.string.length_4, R.string.length_5 };

    public ListViewAdapter(Context context)
    {
        this.context = context;
    }

    public void addItem(File file, Bitmap bitmap, String title, String artist, int length, boolean original, String label, String serial, String dateReleased, String datePurchased, int plays)
    {
        records.add(new Record(file, bitmap, title, artist, length, original, label, serial, dateReleased, datePurchased, plays));
        notifyDataSetChanged();
    }

    public Record removeItem(int pos)
    {
        notifyDataSetChanged();
        return records.remove(pos);
    }

    @Override
    public int getCount()
    {
        return records.size();
    }

    @Override
    public Record getItem(int pos)
    {
        return records.get(pos);
    }

    @Override
    public long getItemId(int pos)
    {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Record record = records.get(position);

        LinearLayoutCompat imageLayout = (LinearLayoutCompat) convertView;
        if (convertView == null)
        {
            imageLayout = (LinearLayoutCompat) LayoutInflater.from(context).inflate(R.layout.list_item_record, null);
        }

        ImageView ivImage = imageLayout.findViewById(R.id.output_image);
        TextView tvTitle = imageLayout.findViewById(R.id.output_title);
        TextView tvArtist = imageLayout.findViewById(R.id.output_artist);
        TextView tvLength = imageLayout.findViewById(R.id.output_length);
        TextView tvOriginal = imageLayout.findViewById(R.id.output_original);
        TextView tvSerial = imageLayout.findViewById(R.id.output_serial);
        TextView tvReleased = imageLayout.findViewById(R.id.output_released);
        TextView tvPurchased = imageLayout.findViewById(R.id.output_purchased);

        ivImage.setImageBitmap(record.bitmap);
        tvTitle.setText(record.title);
        tvArtist.setText(record.artist);
        tvLength.setText(getLength(record));
        tvOriginal.setText(record.original ? "Original" : "");
        tvSerial.setText(record.label + " | " + record.serial);
        tvReleased.setText(context.getString(R.string.field_released) + ": " + record.dateReleased);
        tvPurchased.setText(context.getString(R.string.field_purchased) + ": " + record.datePurchased);

        final TextView tvPlays = imageLayout.findViewById(R.id.output_plays);
        tvPlays.setText(String.valueOf(record.plays));

        Button bPlay = imageLayout.findViewById(R.id.button_play);
        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                tvPlays.setText(String.valueOf(record.play()));
            }
        });

        final int pos = position;
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ((Activity)context).startActivityForResult((new Intent(context, AddRecordActivity.class))
                                .putExtra(context.getString(R.string.field_file), record.file)
                                .putExtra(context.getString(R.string.field_image), record.bitmap)
                                .putExtra(context.getString(R.string.field_title), record.title)
                                .putExtra(context.getString(R.string.field_artist), record.artist)
                                .putExtra(context.getString(R.string.field_length), record.length)
                                .putExtra(context.getString(R.string.field_original), record.original)
                                .putExtra(context.getString(R.string.field_label), record.label)
                                .putExtra(context.getString(R.string.field_serial), record.serial)
                                .putExtra(context.getString(R.string.field_released), record.dateReleased)
                                .putExtra(context.getString(R.string.field_purchased), record.datePurchased)
                                .putExtra(context.getString(R.string.field_plays), record.plays)
                                .putExtra(context.getString(R.string.extra_pos), pos)
                        , MainActivity.REQUEST_RECORD);
            }
        });

        ivImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.dialog_delete)
                        .setMessage(R.string.dialog_delete_2)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                removeItem(pos).file.delete();
                            }})
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {}
                        })
                        .show();
                return true;
            }
        });

        tvPlays.setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { record.plays = 0; tvPlays.setText("0"); }});

        return imageLayout;
    }

    private class SortTitle implements Comparator<Record>
    { @Override public int compare(Record r1, Record r2) { return r1.title.compareTo(r2.title); } }
    private class SortArtist implements Comparator<Record>
    { @Override public int compare(Record r1, Record r2) { return r1.artist.compareTo(r2.artist); } }
    private class SortLength implements Comparator<Record>
    { @Override public int compare(Record r1, Record r2)
        {
            int length_r1 = 0; int length_r2 = 0;
            for (int i = 0; i < length_ids.length; i++)
            {
                if (length_ids[i] == r1.length) length_r1 = i;
                if (length_ids[i] == r2.length) length_r2 = i;
            }
            return length_r1 - length_r2;
        }
    }
    private class SortReleased implements Comparator<Record>
    { @Override public int compare(Record r1, Record r2) { return r1.dateReleased.compareTo(r2.dateReleased); } }
    private class SortPurchased implements Comparator<Record>
    { @Override public int compare(Record r1, Record r2) { return r1.datePurchased.compareTo(r2.datePurchased); } }

    public void sortTitle() { Collections.sort(records, new SortTitle()); notifyDataSetChanged(); }
    public void sortArtist() { Collections.sort(records, new SortArtist()); notifyDataSetChanged(); }
    public void sortLength() { Collections.sort(records, new SortLength()); notifyDataSetChanged(); }
    public void sortReleased() { Collections.sort(records, new SortReleased()); notifyDataSetChanged(); }
    public void sortPurchased() { Collections.sort(records, new SortPurchased()); notifyDataSetChanged(); }

    private String getLength(Record record)
    { for (int i = 0; i < length_ids.length; i++)
        if (length_ids[i] == record.length)
            return context.getString(length_strs[i]);
      return ""; }
}
