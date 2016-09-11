package com.example.by_the_way;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;

public class HomeActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{
	
	private SwipeRefreshLayout mSwipeLayout;
	private ListView first_list;
	private ListView second_list;
	private ListView third_list;
	private String sId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Intent intent=getIntent();
		Bundle data=intent.getExtras();
		sId=(String)data.getString("id");//用户的标识
		first_list=(ListView)findViewById(R.id.first_list);
		second_list=(ListView)findViewById(R.id.home_contact);
		third_list=(ListView)findViewById(R.id.third_task);
		tabInit();
		firstPage();
		secondPage();
		thirdPage();
	}
	private void tabInit(){
		TabHost th=(TabHost)findViewById(R.id.tabhost); 
        th.setup();
        th.addTab(th.newTabSpec("tab1").setIndicator("任务大厅",getResources().getDrawable(R.drawable.ic_launcher)).setContent(R.id.tab1));  
        th.addTab(th.newTabSpec("tab2").setIndicator("联系人",null).setContent(R.id.tab2));  
        th.addTab(th.newTabSpec("tab3").setIndicator("任务详情",null).setContent(R.id.tab3));
	}
	@SuppressWarnings("deprecation")
	private void firstPage(){
		Button pubTask=(Button)findViewById(R.id.pubTask);
		pubTask.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent intent=new Intent(HomeActivity.this,NewTaskActivity.class);
				startActivity(intent);	
			}
		});
		SimpleAdapter adapter=new SimpleAdapter(this,getData("courier_firstPage"+sId),R.layout.task_adapter,
				new String[]{"address","time","img","task_id"},new int[]{R.id.first_address,R.id.first_time,R.id.first_img,R.id.first_task_id});
		first_list.setAdapter(adapter);
		first_list.setOnItemClickListener(new first_ItemClickListener());
		
		mSwipeLayout=(SwipeRefreshLayout)findViewById(R.id.swipeFresh);
		mSwipeLayout.setOnRefreshListener(this); 
		mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
	}
	private void secondPage(){
		SimpleAdapter adapter=new SimpleAdapter(this,getData("student23_contacterQuery"+sId),R.layout.contact_adapter,
				new String[]{"contacter","credit_score"},new int[]{R.id.contacter,R.id.contacter_credit});
		second_list.setAdapter(adapter);
		second_list.setOnItemClickListener(new second_ItemClickListener());
	}
	private void thirdPage(){
		SimpleAdapter adapter=new SimpleAdapter(this,getData("courier_thirdPage"+sId),R.layout.task_adapter,
				new String[]{"address","time","img","task_id"},new int[]{R.id.first_address,R.id.first_time,R.id.first_img,R.id.first_task_id});
		third_list.setAdapter(adapter);
		third_list.setOnItemClickListener(new third_ItemClickListener());
	}
	private List<Map<String, Object>> getData(String page){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String[][] slist=getFreshData(page);
		for(int i=0;i<slist.length;i++){
			map = new HashMap<String, Object>();
			//若是student23和courier增加或减少了字段，要改变这里的i值.
			//firstPage
			if(page.startsWith("courier_")){
			map.put("address",stringTransfer(slist[i][1]));
			map.put("time",slist[i][4]+"+"+slist[i][5]+" hours");
			map.put("img", transferImg(slist[i][0]));
			map.put("task_id","missionID: "+slist[i][7]);
			list.add(map);
			}else if(page.startsWith("student23_")){
			//secondPage
			map.put("credit_score","missionID: "+slist[i][1]);
			map.put("contacter", slist[i][0]);
			list.add(map);
			}
			else{
				map.put(null, null);
				list.add(map);
				return list;
			}
		}
		return list;
	}
	private String[][] getFreshData(String mes){
		acceptThread at=new acceptThread(mes);
		FutureTask<String> task=new FutureTask<String>(at);
		new Thread(task).start();
		String msg=null;
		String[][] slist = null;
		JSONArray ja;
		JSONArray jaTemp;
		try {
			msg=task.get();
			ja=new JSONArray(msg);
			//二维数组两个长度都必须初始化
			slist=new String[ja.length()][];
			for(int i=0;i<ja.length();i++){
				jaTemp=new JSONArray(ja.getString(i));
				slist[i]=new String[jaTemp.length()];
				for(int j=0;j<jaTemp.length();j++){
					slist[i][j]=jaTemp.getString(j);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return slist;
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
	class printThread extends Thread
	{
		String mes;
		Socket socket;
		PrintStream ps;
		public printThread(String mes)
		{
			this.mes=mes;
		}
		public void run()
		{
			try
			{
				socket=new Socket("172.26.151.176",3000);
				ps=new PrintStream(socket.getOutputStream());
				ps.println(mes);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			ps.close();
		}
	}
	public String stringTransfer(String str){
		HashMap<String,String> hp=new HashMap<String,String>();
		hp.put("服装","clothes");
		hp.put("化妆品", "cosmetics");
		hp.put("饰品","accessory");
		hp.put("鞋","shoes");
		hp.put("食物", "food");
		hp.put("龙人宾馆","longrenbinguan");
		hp.put("随心驿站","suixinyizhan");
		hp.put("华贝数码","huabeishuma");
		hp.put("爱心报亭","aixinbaoting");
		hp.put("2号楼", "2#");
		hp.put("3号楼","3#");
		HashMap<String,String> hp1=new HashMap<String,String>();
		for(String key: hp.keySet()){
			hp1.put(hp.get(key),key);
		}
		if(hp.containsKey(str))
			return hp.get(str);
		else
			return hp1.get(str);
	}
	private Object transferImg(String str){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clothes", R.drawable.clothes);
		map.put("shoes", R.drawable.shoes);
		map.put("food", R.drawable.food);
		map.put("accessory",R.drawable.accessory);
		map.put("cosmetics",R.drawable.cosmetics);
		return map.get(str);
	}
	private void makeAlertDialog_OkCancel(final String taskId){
		final Builder builder=new AlertDialog.Builder(this);
		final printThread pt=new printThread("updateGetRen"+sId+taskId);
		builder.setTitle("任务确定");
		builder.setMessage("你确定要领取该任务吗");
		builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pt.start();
				try {
					pt.join();
					//thirdPage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}
	
	
	private final class first_ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			ListView listView=(ListView)parent;
			HashMap<String,Object> data=(HashMap<String,Object>)listView.getItemAtPosition(position);
			String taskId=data.get("task_id").toString();
			makeAlertDialog_OkCancel(taskId);
		}
	}
	private final class second_ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			ListView listView=(ListView)parent;
			HashMap<String,Object> data=(HashMap<String,Object>)listView.getItemAtPosition(position);
			String contacter=data.get("contacter").toString();
			Bundle data1=new Bundle();
			data1.putString("contacter",contacter);
			Intent intent=new Intent(HomeActivity.this,ChatActivity.class);
			intent.putExtras(data1);
			startActivity(intent);
			
		}
	}
	private final class third_ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			ListView listView=(ListView)parent;
			HashMap<String,Object> data=(HashMap<String,Object>)listView.getItemAtPosition(position);
			String taskId=data.get("task_id").toString();
			makeAlertDialog_UndoAchieveCancle(taskId);
		}
	}
	private void makeAlertDialog_UndoAchieveCancle(String taskId){
		Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("完成");
		String[] items=new String[]{"撤销","完成","取消"};
		builder.setItems(items,new third_OnClickListener(taskId));
		builder.create().show();
	}
	class third_OnClickListener implements DialogInterface.OnClickListener{
		private String taskId;
		public third_OnClickListener(String taskId){
			this.taskId=taskId;
		}
		@Override
		public void onClick(DialogInterface dialog, int whitch) {
			//撤销、完成、取消
			switch(whitch){
			case 0:{
				printThread pt=new printThread("courier_UndoTask"+sId+taskId);
				pt.start();
				break;
			}
			case 1:{
				printThread pt=new printThread("courier_AchieveTask"+sId+taskId);
				pt.start();
				break;
			}
			case 2:{
				break;
			}
			}
		}
		
	}
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		firstPage();
		//thirdPage();
		mSwipeLayout.setRefreshing(false);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
