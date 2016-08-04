package lu.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lu.findv.FindV;

import lu.inject.FindVUtil;
import lu.injectview.R;

public class Main2Activity extends AppCompatActivity {

    @FindV(R.id.id_get)
    Button mGetBtn;

    @FindV(R.id.id_main)
    LinearLayout mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        FindVUtil.inject(this);

        mGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContent.addView(new Holder().setSrc(R.drawable.cc).getV());
            }
        });

    }

    public class Holder {

        View v;

        @FindV(R.id.id_image)
        ImageView imageView;

        public Holder() {
            v = getLayoutInflater().inflate(R.layout.item_view, null);
            FindVUtil.inject(this, v);
        }

        public Holder setSrc(int ResId) {
            imageView.setImageResource(ResId);
            return this;
        }

        public View getV() {
            return v;
        }
    }
}
