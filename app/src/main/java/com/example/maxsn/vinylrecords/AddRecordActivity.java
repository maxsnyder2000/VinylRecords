package com.example.maxsn.vinylrecords;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddRecordActivity extends AppCompatActivity
{
    public static final int REQUEST_IMAGE_CAPTURE = 2;

    private Bitmap bitmap = null;
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        final ImageView ivImage = findViewById(R.id.input_image);
        final EditText etTitle = findViewById(R.id.input_title);
        final EditText etArtist = findViewById(R.id.input_artist);
        final RadioGroup rgLength = findViewById(R.id.input_length);
        final CheckBox cbOriginal = findViewById(R.id.input_original);
        final EditText etLabel = findViewById(R.id.input_label);
        final EditText etSerial = findViewById(R.id.input_serial);
        final EditText etReleased = findViewById(R.id.input_released);
        final EditText etPurchased = findViewById(R.id.input_purchased);

        Intent intent = getIntent();
        if (intent.getIntExtra(getString(R.string.field_plays), -1) != -1)
        {
            bitmap = (Bitmap)intent.getParcelableExtra(getString(R.string.field_image));
            ivImage.setImageBitmap(bitmap);

            etTitle.setText(intent.getStringExtra(getString(R.string.field_title)));
            etArtist.setText(intent.getStringExtra(getString(R.string.field_artist)));
            ((RadioButton)findViewById(intent.getIntExtra(getString(R.string.field_length), -1))).setChecked(true);
            cbOriginal.setChecked(intent.getBooleanExtra(getString(R.string.field_original), false));
            etLabel.setText(intent.getStringExtra(getString(R.string.field_label)));
            etSerial.setText(intent.getStringExtra(getString(R.string.field_serial)));
            etReleased.setText(intent.getStringExtra(getString(R.string.field_released)));
            etPurchased.setText(intent.getStringExtra(getString(R.string.field_purchased)));
        }

        findViewById(R.id.button_take_photo).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        findViewById(R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (bitmap == null ||
                        etTitle.getText().toString().equals("") ||
                        etArtist.getText().toString().equals("") ||
                        rgLength.getCheckedRadioButtonId() == -1 ||
                        etLabel.getText().toString().equals("") ||
                        etSerial.getText().toString().equals("") ||
                        etReleased.getText().toString().equals("") ||
                        etPurchased.getText().toString().equals(""))
                {
                    Toast.makeText(AddRecordActivity.this, "Enter in your shit, Max!", Toast.LENGTH_SHORT).show();
                    return;
                }

                File origFile = (File)getIntent().getSerializableExtra(getString(R.string.field_file));
                File file;

                if (imageChanged)
                {
                    file = new File(MainActivity.directory, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_.jpg");

                    try
                    {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    }
                    catch (Exception e) {}

                    if (origFile != null) origFile.delete();
                }
                else
                    file = origFile;

                setResult(RESULT_OK, new Intent()
                        .putExtra(getString(R.string.field_file), file)
                        .putExtra(getString(R.string.field_image), ((BitmapDrawable)ivImage.getDrawable()).getBitmap())
                        .putExtra(getString(R.string.field_title), etTitle.getText().toString())
                        .putExtra(getString(R.string.field_artist), etArtist.getText().toString())
                        .putExtra(getString(R.string.field_length), rgLength.getCheckedRadioButtonId())
                        .putExtra(getString(R.string.field_original), cbOriginal.isChecked())
                        .putExtra(getString(R.string.field_label), etLabel.getText().toString())
                        .putExtra(getString(R.string.field_serial), etSerial.getText().toString())
                        .putExtra(getString(R.string.field_released), etReleased.getText().toString())
                        .putExtra(getString(R.string.field_purchased), etPurchased.getText().toString())
                        .putExtra(getString(R.string.field_plays), getIntent().getIntExtra(getString(R.string.field_plays), 0))
                        .putExtra(getString(R.string.extra_pos), getIntent().getIntExtra(getString(R.string.extra_pos), -1)));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap bmOriginal = ((Bitmap) extras.get("data"));

            bitmap = Bitmap.createBitmap(bmOriginal, 0, bmOriginal.getHeight() / 4, bmOriginal.getWidth(), bmOriginal.getHeight() * 1/2);
            ((ImageView)findViewById(R.id.input_image)).setImageBitmap(bitmap);

            imageChanged = true;
        }
    }
}
