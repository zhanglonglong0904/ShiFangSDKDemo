package com.shifang.demo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shifang.demo.bean.StoredProductBean;
import com.shifang.weight.demo.R;

import java.util.HashMap;
import java.util.Set;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private OnDataChangedListener mOnDataChangedListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private HashMap<String, StoredProductBean> mProductList;
    private Context context;

    private String tag;

    public RecyclerAdapter(Context context){
        this.context = context.getApplicationContext();
    }

    public void setData(HashMap<String, StoredProductBean> dishesMap){
        this.mProductList = dishesMap;
        this.notifyDataSetChanged();
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public HashMap<String, StoredProductBean> getData(){
        return this.mProductList;
    }

    public void clearData(){
        if(mProductList == null){
            return;
        }
        mProductList.clear();

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mProductList == null){
            return 0;
        }

        return mProductList.size();
    }

    public StoredProductBean getItem(int position) {
        Set<String> keySet = mProductList.keySet();
        Object[] keySetArray = keySet.toArray();

        return this.mProductList.get(keySetArray[position]);
    }

    public StoredProductBean getItemByCode(String code){
        return this.mProductList.get(code);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        StoredProductBean storedProductBean = this.getItem(position);

        if(TextUtils.isEmpty(storedProductBean.getCode())){   // 未识别到
            storedProductBean.setName("未知");
            storedProductBean.setPrice(0);
            storedProductBean.setCode("0");
            storedProductBean.setPluNo(0);
//            holder.name.setText("未知");
//            holder.price.setText("0.0");
//            holder.code.setText("0");
        }
        holder.name.setText(String.valueOf(storedProductBean.getName()));
        holder.price.setText(storedProductBean.getPrice() + "元/" + storedProductBean.getPriceUnit());
        holder.code.setText(String.valueOf(storedProductBean.getCode()));

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(v, storedProductBean, position, tag);
                }
            }
        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_recycler, viewGroup, false));
        return holder;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.mOnDataChangedListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, StoredProductBean productBean, int position, String tag);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView price;
        TextView name;
        TextView code;
        ConstraintLayout root;

        public MyViewHolder(View view) {
            super(view);
            name =  view.findViewById(R.id.tv_name);
            price =  view.findViewById(R.id.tv_price);
            code =  view.findViewById(R.id.tv_code);
            root =  view.findViewById(R.id.cv_root);
        }
    }
}