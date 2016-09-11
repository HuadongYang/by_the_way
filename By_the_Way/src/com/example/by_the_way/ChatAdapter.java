package com.example.by_the_way;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {
	private Context context;
	private List<ChatMessages> chatMessages;
	public ChatAdapter(Context context, List<ChatMessages> messages){
		super();
		this.context=context;
		this.chatMessages=messages;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return chatMessages.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return chatMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		ChatMessages message=chatMessages.get(position);
		if(convertView==null || (holder=(ViewHolder)convertView.getTag()).flag!=message.getDirection()){
			holder=new ViewHolder();
			if(message.getDirection()==ChatMessages.Message_from){
				holder.flag=ChatMessages.Message_from;
				convertView=LayoutInflater.from(context).inflate(R.layout.from_chat, null);
				holder.text=(TextView)convertView.findViewById(R.id.from_name);
			}else{
				holder.flag=ChatMessages.Message_to;
				convertView=LayoutInflater.from(context).inflate(R.layout.to_chat, null);
				holder.text=(TextView)convertView.findViewById(R.id.to_name);
			}
			convertView.setTag(holder);
		}
		holder.text.setText(message.getContent());
		return convertView;
	}
	class ViewHolder{
		TextView text;
		int flag;
	}
}
