package com.geekykel.travelnigeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private static final int PICTURE_RESULT = 13;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private ImageView mImageView;
    private Button mUploadImageButton;

    private TravelDeal travelDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        mImageView = findViewById(R.id.image);
        mUploadImageButton = findViewById(R.id.uploadImageBtn);

        Intent intent = getIntent();
        TravelDeal travelDeal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (travelDeal == null) {
            travelDeal = new TravelDeal();
        }
        this.travelDeal = travelDeal;
        editTextTitle.setText(travelDeal.getTitle());
        editTextDescription.setText(travelDeal.getDescription());
        editTextPrice.setText(travelDeal.getPrice());


        showImage(travelDeal.getImageUrl());
        Button btnImage = findViewById(R.id.uploadImageBtn);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);

        if (FirebaseUtil.isAdmin) {
            Toast.makeText(this, "Admin Show Menu", Toast.LENGTH_LONG).show();
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            //findViewById(R.id.uploadImageBtn).setVisibility(View.VISIBLE);
            enableEditTexts(true);
        }
        else {
            Toast.makeText(this, "Not Admin Dont Show Menu", Toast.LENGTH_LONG).show();
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            findViewById(R.id.uploadImageBtn).setVisibility(View.GONE);
            enableEditTexts(false);
        }

        return true;
    }

    private void enableEditTexts(boolean isEnabled) {
        editTextTitle.setEnabled(isEnabled);
        editTextDescription.setEnabled(isEnabled);
        editTextPrice.setEnabled(isEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.save_menu:
                saveDeal();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void clean() {
        editTextTitle.setText("");
        editTextDescription.setText("");
        editTextPrice.setText("");
        editTextTitle.requestFocus();

    }

    private void saveDeal() {

        travelDeal.setTitle(editTextTitle.getText().toString());
        travelDeal.setDescription(editTextDescription.getText().toString());
        travelDeal.setPrice(editTextPrice.getText().toString());

        if (travelDeal.getId() == null) {
            Log.d("FIREBASE", travelDeal.toString());
            mDatabaseReference.push().setValue(travelDeal);
            Log.d("FIREBASE", "Success FireBase");
        } else {
            mDatabaseReference.child(travelDeal.getId()).setValue(travelDeal);
            Log.d("FIREBASE", "Success FireBase Edit");
        }

        Toast.makeText(this, "Deal Saved Successfully! Thank You!!!", Toast.LENGTH_LONG).show();
    }

    private void deleteDeal() {
        if (travelDeal == null){
            Toast.makeText(this, "Deletion Not Successful!", Toast.LENGTH_LONG).show();
            return;
        }

        mDatabaseReference.child(travelDeal.getId()).removeValue();
        Toast.makeText(this, "Deal Deleted Successfully! Thank You!!!", Toast.LENGTH_LONG).show();

        Log.d("FIREBASE image name", travelDeal.getImageName());

        if(travelDeal.getImageName() != null && travelDeal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(travelDeal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("FIREBASE Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("FIREBASE Delete Image", e.getMessage());
                }
            });
        }

    }


    private void backToList() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri urls = uri.getResult();

                    //String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    String url = urls.toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    travelDeal.setImageUrl(url);
                    travelDeal.setImageName(pictureName);
                    Log.d("FIREBASE Url: ", url);
                    Log.d("FIREBASE Name", pictureName);
                    showImage(url);
                }
            });

        }
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(mImageView);
        }
    }

}
