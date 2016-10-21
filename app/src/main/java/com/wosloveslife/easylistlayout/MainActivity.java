package com.wosloveslife.easylistlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wosloveslife.easylistlayout.adapter.BaseRecyclerViewAdapter;
import com.wosloveslife.easylistlayout.viewHolder.BaseRecyclerViewHolder;

public class MainActivity extends AppCompatActivity {

    private EasyList mEasyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEasyList = (EasyList) findViewById(R.id.easy_list_layout);

        Adapter adapter = new Adapter();
        mEasyList.setLayoutManager(new LinearLayoutManager(this));
        mEasyList.setAdapter(adapter);
        TextView view = new TextView(this);
        view.setText("首部数据");
        mEasyList.addHeader(view);

        TextView footer = new TextView(this);
        footer.setText("底部数据");
        mEasyList.addFooter(footer);
    }

    class Adapter extends BaseRecyclerViewAdapter<String> {
        @Override
        protected BaseRecyclerViewHolder<String> onCreateItemViewHolder(ViewGroup parent) {
            return new BaseRecyclerViewHolder<String>(new TextView(parent.getContext())) {
                TextView mTextView;

                @Override
                public void onCreateView(View view) {
                    super.onCreateView(view);
                    mTextView = (TextView) view;
                }

                @Override
                public void onBind(String s, int position) {
                    mTextView.setText("这是测试的数据" + position);
                }
            };
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder<String> holder, int position) {
            holder.onBind(null, position);
        }

        @Override
        public int getRealItemCount() {
            return 50;
        }
    }
}
