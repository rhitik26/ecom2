package com.example.ecom2.Sellers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ecom2.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SellerAddNewProductActivity extends AppCompatActivity {
    private String CategoryName,Description,Price,Pname,saveCurrentDate,saveCurrentTime;
    private Button AddNewProductButton;
    private ImageView InputProductImage;
    private EditText InputProductName,InputProductDescription,InputProductPrice;
    private static final int GalleryPick=1;
    private Uri ImageUri;
    private String ProductRandomKey,DownloadImageUrl;
    private StorageReference ProductImagesRef;
    private DatabaseReference ProductsRef,sellersRef;
    private ProgressDialog loadingBar;
    private String sName,sPhone,sEmail,sAddress,sID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_product);
        CategoryName=getIntent().getExtras().get("category").toString();
        ProductImagesRef= FirebaseStorage.getInstance().getReference().child("Product Images");
        ProductsRef= FirebaseDatabase.getInstance().getReference().child("Products");
        sellersRef= FirebaseDatabase.getInstance().getReference().child("Sellers");
        AddNewProductButton=(Button)findViewById(R.id.add_new_product);
        loadingBar=new ProgressDialog(this);
        InputProductName=(EditText)findViewById(R.id.product_name);
        InputProductDescription=(EditText)findViewById(R.id.product_description);
        InputProductPrice=(EditText)findViewById(R.id.product_price);
        InputProductImage=(ImageView)findViewById(R.id.select_product_image);
        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });
        sellersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            sName=dataSnapshot.child("name").getValue().toString();
                            sID=dataSnapshot.child("sid").getValue().toString();
                            sPhone=dataSnapshot.child("phone").getValue().toString();
                            sEmail=dataSnapshot.child("email").getValue().toString();
                            sAddress=dataSnapshot.child("address").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void ValidateProductData() {
        Description=InputProductDescription.getText().toString();
        Price=InputProductPrice.getText().toString();
        Pname=InputProductName.getText().toString();
        if(ImageUri==null){
            Toast.makeText(SellerAddNewProductActivity.this,"Please select the image",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this,"Please Enter Description",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(Price)){
            Toast.makeText(this,"Please Enter Price",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(Pname)){
            Toast.makeText(this,"Please Enter Name",Toast.LENGTH_LONG).show();
        }
        else {
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {
        loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Please Wait While We are adding new product");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime=currentTime.format(calendar.getTime());
        ProductRandomKey=saveCurrentDate+saveCurrentTime;
        final StorageReference filePath=ProductImagesRef.child(ImageUri.getLastPathSegment()+ProductRandomKey);
        final UploadTask uploadTask=filePath.putFile(ImageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              String message=e.toString();
              Toast.makeText(SellerAddNewProductActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
              loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SellerAddNewProductActivity.this,"Image Added Successfully",Toast.LENGTH_LONG).show();

                Task<Uri>urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        DownloadImageUrl=filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            DownloadImageUrl=task.getResult().toString();
                            Toast.makeText(SellerAddNewProductActivity.this,"Got The Product Image Successfully",Toast.LENGTH_LONG).show();
                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase() {
        HashMap<String,Object>productMap=new HashMap<>();
        productMap.put("pid",ProductRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description",Description);
        productMap.put("image",DownloadImageUrl);
        productMap.put("category",CategoryName);
        productMap.put("price",Price);
        productMap.put("pname",Pname);


        productMap.put("sellerName",sName);
        productMap.put("sellerAddress",sAddress);
        productMap.put("sellerPhone",sAddress);
        productMap.put("sellerEmail",sEmail);
        productMap.put("sid",sID);
        productMap.put("productState","Not Approved");
        ProductsRef.child(ProductRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent=new Intent(SellerAddNewProductActivity.this, SeleersHomeActivity.class);
                            startActivity(intent);
                            loadingBar.dismiss();
                            Toast.makeText(SellerAddNewProductActivity.this,"Product added successfully",Toast.LENGTH_LONG).show();
                        }
                        else {
                            loadingBar.dismiss();
                            String message=task.getException().toString();
                            Toast.makeText(SellerAddNewProductActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri=data.getData();
            InputProductImage.setImageURI(ImageUri);

        }
    }


}
