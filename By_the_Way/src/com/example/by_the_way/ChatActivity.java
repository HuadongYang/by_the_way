package com.example.by_the_way;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends Activity implements OnClickListener{
	private String contacter;
	private ChatAdapter chatHistoryAdapter;
	private List<ChatMessages> messages=new ArrayList<ChatMessages>();
	static Handler handler;
	private EditText send_text;
	//private Socket socket;
	ListView listview;
	Button send_button;
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_chat);
		send_text=(EditText)findViewById(R.id.send_text);
		send_button=(Button)findViewById(R.id.send_button);
		send_button.setOnClickListener(this);
		listview=(ListView)findViewById(R.id.chatting);
		init_title();//初始化标题栏
		chatHistoryAdapter=new ChatAdapter(this,messages);
		listview.setAdapter(chatHistoryAdapter);
		new ClientThread().start();
		handler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				if(msg.what==0x123){
					messages.add(new ChatMessages(ChatMessages.Message_to,msg.obj.toString()));
					chatHistoryAdapter.notifyDataSetChanged();
				}
			}
		};
		
	}
	private void init_title(){
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		Intent intent=getIntent();
		Bundle data=intent.getExtras();
		contacter=(String)data.getString("contacter");//用户的标识
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.chat_title);  
		TextView from_id_text=(TextView)findViewById(R.id.chatting_contact_name);
		from_id_text.setText(contacter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId()==R.id.send_button){
			String content=send_text.getText().toString().trim();
			if(!content.matches("\\s") && !content.equals("")){
				new printThread(content).start();
				send_text.setText("");
				messages.add(new ChatMessages(ChatMessages.Message_from,content));
				chatHistoryAdapter.notifyDataSetChanged();
			}
		}
	}
	
}
class printThread extends Thread{
	//private Socket socket;
	private String content;
	private PrintStream ps;
	private Socket socket=ClientThread.socket;
	public printThread(String content){
		this.content=content;
	}
	public void run(){
		try {
			ps = new PrintStream(socket.getOutputStream());
			ps.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
class ClientThread extends Thread{
	static Socket socket;

	public void run(){
		try
		{
			socket=new Socket("172.26.151.176",3000);
			try{
				BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String content=null;
				//不允许在副线程对组件改变，通过handler
				while((content=br.readLine())!=null){
					Message msg=new Message();
					msg.what=0x123;
					msg.obj=content;
					ChatActivity.handler.sendMessage(msg);
				}
			}catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}