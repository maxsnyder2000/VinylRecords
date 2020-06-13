package com.example.maxsn.vinylrecords;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_RECORD = 1;

    private ListView listView;
    private ListViewAdapter adapter;

    public static File directory;
    private File data_file;

    private File data_backup_file;
    private boolean restoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        directory = new File(getExternalFilesDir(null) + File.separator);
        directory.mkdirs();

        data_file = new File(directory, getString(R.string.file_data));
        data_backup_file = new File(directory, getString(R.string.file_backup));

        listView = findViewById(R.id.list_view);
        adapter = new ListViewAdapter(this);
        listView.setAdapter(adapter);

        View footer = View.inflate(MainActivity.this, R.layout.footer_view, null);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_backup)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                try { copyFile(data_file, data_backup_file); }
                                catch (IOException e) {}

                                Toast.makeText(MainActivity.this, getString(R.string.toast_backup), Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {}
                        })
                        .show();
            }
        });
        footer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_restore)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                restoring = true;

                                try { copyFile(data_backup_file, data_file); }
                                catch (IOException e) {}

                                recreate();
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
        restoring = false;
        listView.addFooterView(footer);

        findViewById(R.id.button_stitle).setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { adapter.sortTitle(); }});

        findViewById(R.id.button_sartist).setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { adapter.sortArtist(); }});

        findViewById(R.id.button_slength).setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { adapter.sortLength(); }});

        findViewById(R.id.button_sreleased).setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { adapter.sortReleased(); }});

        findViewById(R.id.button_spurchased).setOnClickListener(new View.OnClickListener()
        { @Override public void onClick(View view) { adapter.sortPurchased(); }});
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (adapter.getCount() == 0)
            new AddExistingFilesToAdapterTask().execute();

        updateCount();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!restoring) writeData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_record_button)
        {
            startActivityForResult(new Intent(MainActivity.this, AddRecordActivity.class), REQUEST_RECORD);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_RECORD && resultCode == RESULT_OK)
        {
            int pos = data.getIntExtra(getString(R.string.extra_pos), -1);
            if (pos != -1) adapter.removeItem(pos);

            adapter.addItem((File)data.getSerializableExtra(getString(R.string.field_file)),
                    (Bitmap) data.getParcelableExtra(getString(R.string.field_image)),
                    data.getStringExtra(getString(R.string.field_title)),
                    data.getStringExtra(getString(R.string.field_artist)),
                    data.getIntExtra(getString(R.string.field_length), -1),
                    data.getBooleanExtra(getString(R.string.field_original), true),
                    data.getStringExtra(getString(R.string.field_label)),
                    data.getStringExtra(getString(R.string.field_serial)),
                    data.getStringExtra(getString(R.string.field_released)),
                    data.getStringExtra(getString(R.string.field_purchased)),
                    data.getIntExtra(getString(R.string.field_plays), 0));
        }
    }

    private void updateCount() { ((TextView)findViewById(R.id.num_records)).setText("NUMBER OF RECORDS: " + adapter.getCount() + "."); }

    private void writeData()
    {
        PrintWriter writer = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(data_file);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));

            for (int idx = 0; idx < adapter.getCount(); idx++)
            {
                ListViewAdapter.Record record = adapter.getItem(idx);
                writer.println(record.file);
                writer.println(record.title);
                writer.println(record.artist);
                writer.println(record.length);
                writer.println(record.original ? getString(R.string.file_true) : getString(R.string.file_false));
                writer.println(record.label);
                writer.println(record.serial);
                writer.println(record.dateReleased);
                writer.println(record.datePurchased);
                writer.println(record.plays);
            }
        }
        catch (IOException e) { Log.e("e", "e"); }
        finally
        {
            if (null != writer) writer.close();
        }
    }

    private class AddExistingFilesToAdapterTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            BufferedReader reader = null;
            try
            {
                FileInputStream fis = new FileInputStream(data_file);
                reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                String filePath;
                while (null != (filePath = reader.readLine()))
                {
                    adapter.addItem(new File(filePath),
                            BitmapFactory.decodeFile(filePath, options),
                            reader.readLine(),
                            reader.readLine(),
                            Integer.parseInt(reader.readLine()),
                            reader.readLine().equals(getString(R.string.file_true)),
                            reader.readLine(),
                            reader.readLine(),
                            reader.readLine(),
                            reader.readLine(),
                            Integer.parseInt(reader.readLine()));
                }
            }
            catch (FileNotFoundException e) {}
            catch (IOException e) {}
            finally
            {
                if (null != reader)
                {
                    try { reader.close(); }
                    catch (IOException e) {}
                }
            }

            updateCount();

            return null;
        }
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try
        {
            FileInputStream fis = new FileInputStream(src);
            reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));

            FileOutputStream fos = new FileOutputStream(dst);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));

            String line;
            while (null != (line = reader.readLine()))
            {
                writer.println(line);
            }
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally
        {
            if (null != reader)
            {
                try { reader.close(); }
                catch (IOException e) {}
            }
            if (null != writer) writer.close();
        }
    }
}
