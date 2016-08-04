package lu.injectview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lu.findv.FindV;

import lu.activity.Main2Activity;
import lu.inject.FindVUtil;

public class MainActivity extends AppCompatActivity {

    @FindV(R.id.id_btn1)
    Button mBtn1;

    @FindV(R.id.id_btn2)
    Button mBtn2;

    @FindV(R.id.id_btn3)
    Button mBtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FindVUtil.inject(this);
        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }


}
