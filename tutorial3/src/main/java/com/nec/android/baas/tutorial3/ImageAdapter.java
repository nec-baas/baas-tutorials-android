package com.nec.android.baas.tutorial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nec.baas.core.NbErrorInfo;
import com.nec.baas.file.NbFileDownloadCallback;

import java.io.File;
import java.util.List;

public class ImageAdapter extends ArrayAdapter<String> {
    private int mResourceId;
    Context mContext;
    
    Handler mHandler = new Handler();

    public ImageAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mResourceId = resource;
        mContext = context;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResourceId,  null);
        }
        
        String name = getItem(position);
        
        //名前のセット
        TextView text =(TextView)convertView.findViewById(R.id.name);
        text.setText(name);
        
        ImageTask task = new ImageTask(name, (ImageView)convertView.findViewById(R.id.image));
        task.execute();
        
        return convertView;
    }
    
    class ImageTask extends AsyncTask<Void, Void, Void> {
        String mName;
        ImageView mView;
        
        ImageTask(String name, ImageView view){
            mName = name;
            mView = view;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            File file = new File(mContext.getFilesDir().getAbsolutePath() + "/" + mName);
            ((MainActivity)mContext).getFileBucket().downloadFile(mName, file, new NbFileDownloadCallback(){
                @Override
                public void onSuccess(String path) {
                    drawImage(path);
                }
                
                @Override
                public void onFailure(int status, NbErrorInfo ei) {
                    ((MainActivity)mContext).showAlert("download failed : " + status);  
                }
            });
            
            return null;
        }

        private void drawImage(final String imagePath) {
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    mView.setImageBitmap(bitmap);
                }
            });
        }
    }
}
