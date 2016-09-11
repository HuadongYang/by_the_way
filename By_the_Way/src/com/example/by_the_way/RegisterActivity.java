package com.example.by_the_way;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		Button bnRe=(Button)findViewById(R.id.register_r);
		final EditText student_idText=(EditText)findViewById(R.id.accountEdittext_r);
		final EditText phoneText=(EditText)findViewById(R.id.numEdittext);
		phoneText.setInputType(InputType.TYPE_CLASS_PHONE);
		final Builder builder=new AlertDialog.Builder(this);
		final EditText passwordText=(EditText)findViewById(R.id.passwordEdittext);
		final EditText ensurePassText=(EditText)findViewById(R.id.ensurePasswordEdittext);
		Button bnCa=(Button)findViewById(R.id.cancel_r);
		
		
		bnRe.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				
				int flag=0;
				final String msg;
				//文本框的信息
				String student_id="id_"+student_idText.getText().toString().trim();
				String phone="ph_"+phoneText.getText().toString().trim();
				String password="pw_"+passwordText.getText().toString().trim();
				String ensurePass="pw_"+ensurePassText.getText().toString().trim();
				
				//两个密码不一致
				if(!password.equals(ensurePass))	
				{
					passwordText.setText("");
					ensurePassText.setText("");
					Toast toast=Toast.makeText(RegisterActivity.this, "两次密码输入不一样", Toast.LENGTH_SHORT);
					toast.show();
				}
				//密码为空
				else if(password.isEmpty())
				{
					Toast toast=Toast.makeText(RegisterActivity.this, "密码不得为空", Toast.LENGTH_SHORT);
					toast.show();
				}
				//发送到服务器端
				else
				{					
					msg="regis_"+student_id+","+phone+","+password;
					Toast toast=Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG);
					toast.show();
					ClientThread ct=new ClientThread(msg);
					FutureTask<Integer> task=new FutureTask<Integer>(ct);
					new Thread(task).start();
					
					//三种：0、没有连网；1、学号不存在；2、学号和电话不匹配；3、已注册；4、成功；
					
					try {
						flag=task.get();
					} catch (InterruptedException e) {						
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					switch(flag)
					{
						case 5:
							builder.setTitle("消息提示");
							builder.setMessage("连接超时");
							builder.setPositiveButton("确定", null);
							builder.show();
							break;
							
						case 4:						
							builder.setTitle("消息提示");
							builder.setMessage("成功注册");
							builder.setPositiveButton("确定", null);
							builder.show();
							break;
						
						case 1:						
							builder.setTitle("消息提示");
							builder.setMessage("学号不存在");
							builder.setPositiveButton("确定", null);
							builder.show();
							break;
						
						case 2:						
							builder.setTitle("消息提示");
							builder.setMessage("学号和电话不匹配");
							builder.setPositiveButton("确定", null);
							builder.show();
						
						case 3:						
							builder.setTitle("消息提示");
							builder.setMessage("之前已被注册");
							builder.setPositiveButton("确定", null);
							builder.show();
							break;
						
						case 0:						
							builder.setTitle("消息提示");
							builder.setMessage("连不上服务器");
							builder.setPositiveButton("确定", null);
							builder.show();
							break;
							
						default:
							builder.setTitle("消息提示");
							builder.setMessage("未知原因");
							builder.setPositiveButton("确定", null);
							builder.show();
						
					}
					
				}
			}
		});
		bnCa.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
				startActivity(intent);
			}
	
		});
	}
	class ClientThread implements Callable<Integer>
	//class ClientThread implements Runnable
	 {
	 	Socket socket;
	 	private String msg;
	 	int flag=0;
	 	//EditText flagShow;
	 	public ClientThread (String msg)
	 	{
	 		this.msg=msg;
	 		//this.flagShow=flagShow;
	 	}

		public Integer call() throws InterruptedException, ExecutionException
	 	{
	 		
	 		try
	 		{
	 			//设置连接时间
	 			socket=new Socket("172.26.151.133",3000);
	 			socket.setSoTimeout(3000);
	 			PrintStream os=new PrintStream(socket.getOutputStream());
	 			os.println(msg);
	 			InputThread it=new InputThread(socket);
	 			FutureTask<Integer> task=new FutureTask<Integer>(it);
				new Thread(task).start();
				flag=task.get();
	 			return flag;
	 			
	 		}
	 		catch(SocketTimeoutException ex)
	 		{
	 			return flag;
	 		}
	 		catch(IOException e)
	 		{
	 			e.printStackTrace();
	 			return flag;
	 		}
	 	}
	 }
	class InputThread implements Callable<Integer>
	{
		private Socket socket;
		BufferedReader br=null;
		private int flag=0;
		public InputThread(Socket socket) throws IOException
		{
			this.socket=socket;
			br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		public Integer call()
		{
			try
			{
				String line=null;
				if((line=br.readLine())!=null)
				{
					flag=Integer.parseInt(line);
				}
				return flag;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return flag;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
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
}
