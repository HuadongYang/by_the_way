package com.example.by_the_way;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.example.by_the_way.HomeActivity.acceptThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LoginActivity extends Activity implements OnClickListener{
	Button login;
	Button register;
	EditText account;
	EditText password;
	Builder builder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		login=(Button)findViewById(R.id.login_login);
		register=(Button)findViewById(R.id.login_register);
		account=(EditText)findViewById(R.id.login_accE);
		password=(EditText)findViewById(R.id.login_pwE);
		builder=new AlertDialog.Builder(this);
		login.setOnClickListener(this);
		register.setOnClickListener(this);
	}
		
	
	
	@Override
	public void onClick(View arg0){
		switch(arg0.getId()){
		case R.id.login_login:{
			login();
			break;
		}
		case R.id.login_register:{
			Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
			startActivity(intent);
			break;
		}
		}
	}
	private void login(){
		int flag=0;
		String sId=account.getText().toString().trim();
		String pwd=password.getText().toString().trim();
		if(sId.substring(3).isEmpty() | pwd.substring(3).isEmpty())
		{
			Toast toast=Toast.makeText(LoginActivity.this, "不得为空", Toast.LENGTH_SHORT);
			toast.show();
		}
		else
		{
			flag=getFlag();
			switch(flag)
			{
				case 4:
					builder.setTitle("消息提示");
					builder.setMessage("没有此学号");
					builder.setPositiveButton("确定", null);
					builder.show();
					break;
				case 1:
					builder.setTitle("消息提示");
					builder.setMessage("尚未注册");
					builder.setPositiveButton("确定", null);
					builder.show();
					break;
				case 2:
					builder.setTitle("消息提示");
					builder.setMessage("密码错误");
					builder.setPositiveButton("确定", null);
					builder.show();
					break;
				case 3:
					Bundle data=new Bundle();
					data.putString("id",sId);
					Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
					intent.putExtras(data);
					startActivity(intent);
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
					break;
			}
			
		}
	}
	public int getFlag(){
		int flag=0;
		String sId=account.getText().toString().trim();
		String pwd=password.getText().toString().trim();
		if(sId.substring(3).isEmpty() | pwd.substring(3).isEmpty())
		{
			Toast toast=Toast.makeText(LoginActivity.this, "不得为空", Toast.LENGTH_SHORT);
			toast.show();
		}
		else
		{
			String msg="student23_login_"+sId+pwd;
			acceptThread at=new acceptThread(msg);
			FutureTask<String> task=new FutureTask<String>(at);
			new Thread(task).start();
			//flag==4：没有此学号；flag==1，尚未注册；flag=2，密码不对；flag=3，登录成功。
			try {
				flag=Integer.valueOf(task.get());
			} catch (InterruptedException e) {						
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
	class acceptThread implements Callable<String>  //接收的最后的信息应该是end作为接受结束的标志
	{
		String mes="";
		String msg;
		Socket socket;
		PrintStream ps;
		BufferedReader br;
		public acceptThread(String msg){
			this.msg=msg;
		}
		public String call()
		{
			try
			{
				socket=new Socket("172.26.151.176",3000);
				ps=new PrintStream(socket.getOutputStream());
				ps.println(msg);
				br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(true){
					String temp=br.readLine();
					if(temp.equals("end")){
						break;
					}
					mes+=temp;
				}
				br.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			return mes;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
