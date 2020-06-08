package com.example.ecom2.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ecom2.Buyers.HomeActivity;
import com.example.ecom2.Buyers.MainActivity;
import com.example.ecom2.R;
import com.example.ecom2.Sellers.SellerProductCategoryActivity;

public class AdminHomeActivity extends AppCompatActivity {
    private Button LogoutBtn,CheckOrderBtn,maintainProductsBtn,checkApproveProductsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        LogoutBtn=(Button)findViewById(R.id.admin_logout_btn);
        CheckOrderBtn=(Button)findViewById(R.id.check_orders_btn);
        maintainProductsBtn=(Button)findViewById(R.id.maintain_btn);
        checkApproveProductsBtn=(Button)findViewById(R.id.check_approve_products_btn);

        maintainProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, HomeActivity.class);
                intent.putExtra("Admin","Admin");
                startActivity(intent);
            }
        });

        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        CheckOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, AdminNewOrdersActivity.class);
                startActivity(intent);

            }
        });
        checkApproveProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, AdminCheckNewProductsActivity.class);
                startActivity(intent);

            }
        });
    }
}
