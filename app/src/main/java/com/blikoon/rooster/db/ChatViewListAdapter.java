package com.blikoon.rooster.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blikoon.rooster.FontType;
import com.blikoon.rooster.R;
import com.blikoon.rooster.Utils;
import com.bumptech.glide.Glide;
import com.example.drawingdemo.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 4/11/2017.
 */

public class ChatViewListAdapter extends BaseAdapter {

    ArrayList<ChatMessage> chatMessages;
    Context mContext;
    LayoutInflater mInflater;

    public final int STATUS_SENT = 0;
    public final int STATUS_RECEIVED = 1;
    public boolean todayTextOneTimeFlag=false;
    public static Typeface roboto_regular;

    public ChatViewListAdapter(Context context) {
        mContext = context;
        chatMessages = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        roboto_regular=Utils.getCustomFont(context, FontType.ROBOTO_REGULAR);
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getStatus().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ChatMessage.Status status = chatMessages.get(position).getStatus();

        int type = getItemViewType(position);
        ViewHolder holder;

        if (convertView == null) {
            switch (type) {
                case STATUS_SENT:
                    convertView = mInflater.inflate(/*co.devcenter.androiduilibrary.*/R.layout.chat_item_sent_1, parent, false);
                    break;
                case STATUS_RECEIVED:
                    convertView = mInflater.inflate(/*co.devcenter.androiduilibrary.*/R.layout.chat_item_rcv_1, parent, false);
                    break;
            }

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.getMessageTextView().setText(chatMessages.get(position).getMessage());
        if(chatMessages.get(position).getFormattedTime().split("-").length>1)
            holder.getTimestampTextView().setText(chatMessages.get(position).getFormattedTime().split("-")[1]);
        else
            holder.getTimestampTextView().setText(chatMessages.get(position).getFormattedTime().split("-")[0]);

        holder.getUserNameTextView().setText(chatMessages.get(position).getUsername());
        Log.d("dateStamp",chatMessages.get(position).getFormattedTime().split("-")[0]);
        holder.getDatestampTextView().setText(chatMessages.get(position).getFormattedTime().split("-")[0]);
        if(position>0 &&
                !chatMessages.get(position).getFormattedTime().split("-")[0].equalsIgnoreCase(chatMessages.get(position-1).getFormattedTime().split("-")[0]) &&
                chatMessages.get(position).getFormattedTime().split("-").length>1){
            holder.getDatestampTextView().setVisibility(View.VISIBLE);
        }else if(position==0 && chatMessages.get(position).getFormattedTime().split("-").length>1){
            holder.getDatestampTextView().setVisibility(View.VISIBLE);
        }
        else{
            holder.getDatestampTextView().setVisibility(View.GONE);
        }


        /*MESSAGE DELIVERY STATUS*/
        if(chatMessages.get(position).getDeliveryStatus()==0)
        holder.getDeliveryStatusImageView().setImageResource(R.drawable.message_undeliver_icon);
        else if(chatMessages.get(position).getDeliveryStatus()==1)
            holder.getDeliveryStatusImageView().setImageResource(R.drawable.message_delivered_icon);
        else if(chatMessages.get(position).getDeliveryStatus()==2)
            holder.getDeliveryStatusImageView().setImageResource(R.drawable.message_seen_icon);
        else if(chatMessages.get(position).getDeliveryStatus()==-1)
            holder.getDeliveryStatusImageView().setImageResource(R.drawable.ic_action_clock);

        String docsFileName = chatMessages.get(position).getDocumentFileName();

        if(docsFileName!=null) {
            String arr[]=docsFileName.split("/");
            String OnlyFileName=arr[arr.length-1];
            holder.getFileNameTextView().setText(OnlyFileName);

                if(chatMessages.get(position).getDocumentFileName().contains("txt"))
                    holder.getChatFileImageView().setImageResource(R.drawable.text_file_icon);
                else if(chatMessages.get(position).getDocumentFileName().contains("xls") || chatMessages.get(position).getDocumentFileName().contains("xlsx"))
                    holder.getChatFileImageView().setImageResource(R.drawable.xls_file_icon);
                else if(chatMessages.get(position).getDocumentFileName().contains("zip"))
                    holder.getChatFileImageView().setImageResource(R.drawable.zip);
                else if(chatMessages.get(position).getDocumentFileName().contains("doc"))
                    holder.getChatFileImageView().setImageResource(R.drawable.doc_file_icon);
                else if(chatMessages.get(position).getDocumentFileName().contains("pdf"))
                    holder.getChatFileImageView().setImageResource(R.drawable.pdf_file_icon);

        }

        if(chatMessages.get(position).getImageFileName()!=null && !chatMessages.get(position).getImageFileName().equalsIgnoreCase("")){
    //        holder.getChatImageView().setImageBitmap(chatMessages.get(position).getImageBitmap());
           /* ByteArrayOutputStream stream = new ByteArrayOutputStream();
            chatMessages.get(position).getImageBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(holder.getChatImageView().getContext()).load(stream.toByteArray())
                    .asBitmap()
                    .into(holder.getChatImageView());*/

            Glide.with(mContext)
                    .load(new File(chatMessages.get(position).getImageFileName()))
                    .into(holder.getChatImageView());

            holder.getMessageTextView().setVisibility(View.GONE);
            holder.getChatImageView().setVisibility(View.VISIBLE);
            holder.getFileLayoutView().setVisibility(View.GONE);
        }
        else if(chatMessages.get(position).getDocumentFileName()!=null) {
            holder.getFileLayoutView().setVisibility(View.VISIBLE);
            holder.getMessageTextView().setVisibility(View.GONE);
            holder.getChatImageView().setVisibility(View.GONE);
        }
        else{
            holder.getMessageTextView().setVisibility(View.VISIBLE);
            holder.getChatImageView().setVisibility(View.GONE);
            holder.getFileLayoutView().setVisibility(View.GONE);
        }



        holder.getChatImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatMessages.get(position).getImageFileName()!=null && !chatMessages.get(position).getImageFileName().equalsIgnoreCase("")){
                    Utils.showFullImageDialog(mContext, Utils.loadSampledImage(chatMessages.get(position).getImageFileName()));
                }/*else if(chatMessages.get(position).getDocumentFileName()!=null){
                    Utils.openFile(mContext,chatMessages.get(position).getDocumentFileName());
                }*/
            }
        });

        holder.getFileLayoutView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if((chatMessages.get(position).getImageBitmap()!=null)){
                    Utils.showFullImageDialog(mContext,chatMessages.get(position).getImageBitmap());
                }else*/ if(chatMessages.get(position).getDocumentFileName()!=null){
                    Utils.openFile(mContext,chatMessages.get(position).getDocumentFileName());
                }
            }
        });

        return convertView;
    }


    static class ViewHolder {
        View row,fileLayoutView;
        TextView messageTextView;
        TextView timestampTextView,dateStampTextView,usernameTextView,fileNameTextView;
        ImageView chatImageView,fileImageViewIcon,deliverStatusIcon;

        public ViewHolder(View convertView) {
            row = convertView;
        }

        public TextView getMessageTextView() {
            if (messageTextView == null) {
                messageTextView = (TextView) row.findViewById(R.id.message_text_view);
                messageTextView.setTypeface(roboto_regular);
            }

            return messageTextView;
        }

        public TextView getTimestampTextView() {
            if (timestampTextView == null) {
                timestampTextView= (TextView) row.findViewById(R.id.timestamp_text_view);
                timestampTextView.setTypeface(roboto_regular);

            }

            return timestampTextView;
        }

        public TextView getDatestampTextView() {
            if (dateStampTextView == null) {
                dateStampTextView= (TextView) row.findViewById(R.id.dateStamp);
            }

            return dateStampTextView;
        }

        public TextView getUserNameTextView() {
            if (usernameTextView == null) {
                usernameTextView= (TextView) row.findViewById(R.id.userNameText);
            }

            return usernameTextView;
        }
        public ImageView getChatImageView() {
            if (chatImageView == null) {
                chatImageView= (ImageView) row.findViewById(R.id.img_chat);
            }

            return chatImageView;
        }
        public ImageView getChatFileImageView() {
            if (fileImageViewIcon == null) {
                fileImageViewIcon= (ImageView) row.findViewById(R.id.file_icon);
            }

            return fileImageViewIcon;
        }
        public TextView getFileNameTextView() {
            if (fileNameTextView == null) {
                fileNameTextView= (TextView) row.findViewById(R.id.fileNameText);
            }

            return fileNameTextView;
        }

        public View getFileLayoutView() {
            if (fileLayoutView == null) {
                fileLayoutView= row.findViewById(R.id.file_view_layout);
            }

            return fileLayoutView;
        }

        public ImageView getDeliveryStatusImageView() {
            if (deliverStatusIcon == null) {
                deliverStatusIcon= (ImageView) row.findViewById(R.id.delivery_status_image_icon);
            }

            return deliverStatusIcon;
        }
    }


    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyDataSetChanged();
    }

    public void clearChatView(){
        chatMessages.clear();
        notifyDataSetChanged();
    }

    public void refreshChatView(ArrayList<ChatMessage> list){
        chatMessages.clear();
        chatMessages.addAll(list);
        notifyDataSetChanged();
    }
}

