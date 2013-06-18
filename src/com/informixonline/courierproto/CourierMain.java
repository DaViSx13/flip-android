package com.informixonline.courierproto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.informixonline.courierproto.OrderDbAdapter;
import com.informixonline.courierproto.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) 
public class CourierMain extends Activity implements OnClickListener {
	
	static long ordersId; // ID ������ ���������� � ������ ��� ��������� ����� CustomAdapter
	private byte SQLSORTORDER = 0; // ���� �������� ����������: 0 desc 1 asc ��� ����������� (����� ������� ������� ������ ��������) 

	// ����� ������� �������� (������������ � ActSettings � NetWorker) ��� ������� � �������� ��������
	final static String SHAREDPREF = "sharedstore";
	final static String APPCFG_LOGIN = "LOGIN"; // ���������� ����� � ���������, ����� ����������, ������� ������ ��������� ������ ��� ���
	final static String APPCFG_LOGIN_URL = "LOGIN_URL";
	final static String APPCFG_GETDATA_URL = "GETDATA_URL";
	final static String APPCFG_ADDR_URL = "ADDRURL";
	
	// ���� �������� ��� ��������� ���������� Activity Return Code - ARC
	final int ARC_NUMITEMS = 1; // �������� ���-�� ����������� 
	final int ARC_POD = 2; // �������� ���
	
	Cursor cursor;
	ListView listView;
	NetWorker nwork = new NetWorker();
	
	// ������ ���������� ��� �������� � ��������� ����� 
	static String recType_forDetail = "N"; // 0 - �����, 1 - ���������, 2 - ����
	static String orderDetail_aNO;
	static String tvDorder_state_ordStatus;
	static String tvDorder_type_ordType;
	static String tvDacash;
	static String tvDaddr_aAddress;
	static String tvDcomp_name_client;
	static String tvDcontact_Cont;
	static String tvDcontact_num_ContPhone;
	static String tvDtimeBE;
	static String tvDpos_num_Packs;
	static String tvDweight_Wt;
	static String tvDvol_weight_VolWt;
	static String tvDcomment;
	static String tvLocNumItems;
	static String tvDIsredy;
	static String tvDInway;

	// �������������� ������������ ���� ������ (��� ���������� ������� �� ������� ������)
	private static final int CM_CATCH_ORDER = 0;
	private static final int CM_RET_ORDER = 1;
	private static final int CM_BACK_ORDER = 2;
	
	private OrderDbAdapter dbHelper;
	// private SimpleCursorAdapter dataAdapter;
	private MyCursorAdapter dataAdapter;
	
	// ������ ������� ��������
	Button btnAddr, btnClient, btnTime, btnExit, btnInWay, btnOk, btnPod, btnDetail, btnNumItems, btnAll; //btnSettings
	
	TextView tvCourName, tvRefrTime, tvNewRecs, tvAllRecs; // tvNewAllRecs ��������� ������
	ImageView imgvSrvOff, imgvSrvOn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courier_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// �������� ����������� ������� ���������
    	SharedPreferences sharedAppConfig;
    	sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
    	this.login_URL = sharedAppConfig.getString(APPCFG_LOGIN_URL, "");
    	this.getdata_URL = sharedAppConfig.getString(APPCFG_GETDATA_URL, "");
    	this.prev_user = sharedAppConfig.getString(APPCFG_LOGIN, "nologin");
		Log.d("CourierMain.getNetworkData", "Login URL = " + login_URL + " GetData URL = " + getdata_URL);
		
		// ������ �������
		tvCourName = (TextView)findViewById(R.id.tvCourName);
		tvRefrTime = (TextView)findViewById(R.id.tvRefrTime);
		//tvNewAllRecs = (TextView)findViewById(R.id.tvNewAllRecs);
		//tvNewAllRecs.setText("");
		
		tvAllRecs = (TextView)findViewById(R.id.tvAllRecs);
		tvAllRecs.setText("");
		tvNewRecs = (TextView)findViewById(R.id.tvNewRecs);
		tvNewRecs.setText("");
		
		imgvSrvOn = (ImageView)findViewById(R.id.imgvSrvOn);
		imgvSrvOff = (ImageView)findViewById(R.id.imgvSrvOff);
		imgvSrvOff.setVisibility(View.INVISIBLE);
		

		// ���������� ������ ������ � ���� �����
		//boolean res = false;
		showLogin();
		
        // ��������� �������� ������ � ����� � ������� UI ������
        StrictMode.ThreadPolicy policy = new StrictMode.
        		ThreadPolicy.Builder().permitAll().build();
        		StrictMode.setThreadPolicy(policy);
        
        Log.d("CourierMain", "--- After showLogin()");
		dbHelper = new OrderDbAdapter(this);
		dbHelper.open();

		// Clean all data
		//dbHelper.deleteAllOrders(); // ������� ������ ������ ����� �������
		// Add some data
		//dbHelper.insertTestEntries();
		//Log.d("POST", "--- DELETE ALL orders before connect ---");

		
		// ���������� ������ ������������ � ������� ���� ��� ���� ������ ����� ��������� ������
		// sendOfflineData();
		
		// ������ �� ������� ��������
		btnAddr = (Button)findViewById(R.id.btnAddr);
		btnAddr.setOnClickListener(this);
		
		btnClient = (Button)findViewById(R.id.btnClient);
		btnClient.setOnClickListener(this);
		
		btnTime = (Button)findViewById(R.id.btnTime);
		btnTime.setOnClickListener(this);
		
		//btnSettings = (Button)findViewById(R.id.btnSaveSet);
		//btnSettings.setOnClickListener(this);
		
		btnExit = (Button)findViewById(R.id.btnExit);
		btnExit.setOnClickListener(this);
		
		btnInWay = (Button)findViewById(R.id.btnInWay);
		btnInWay.setOnClickListener(this);
		btnOk = (Button)findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		btnPod = (Button)findViewById(R.id.btnPod);
		btnPod.setOnClickListener(this);
		btnDetail = (Button)findViewById(R.id.btnDetail);
		btnDetail.setOnClickListener(this);
		btnNumItems = (Button)findViewById(R.id.btnNumItems);
		btnNumItems.setOnClickListener(this);
		btnAll = (Button)findViewById(R.id.btnAll);
		btnAll.setOnClickListener(this);	
		// Generate ListView from SQLite Database
		// displayListView(); moved to dialog
	}
	
	// ��������� ����������� �� ���.�������� � ������� �������� (���.�� ���� requestCode)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ARC_NUMITEMS:
			// �������� ���-�� ����������� - ��������� ���� ������ ��
			if (resultCode == RESULT_OK) {
				String numItems = data.getStringExtra("numitems");
				long rowid = data.getLongExtra("ordersid", 0);
				Log.d("CourierMain.onActivityResult", "numItems = " + numItems
						+ " rowid = " + rowid);
				dbHelper.updLocNumItems(rowid, numItems);
				// ��������� ������
				cursor.requery();
				dataAdapter.swapCursor(dbHelper.fetchModOrders());
				dataAdapter.notifyDataSetChanged();
			} else {
				Log.d("CourierMain.onActivityResult", "Numitems Result cancel");
			}
			break;
			
		case ARC_POD:
			if (resultCode == RESULT_OK) {
				// �������� ��� (������������� ��� ���������)
				Log.d("CourierMain.onActivityResult",
						"Result from POD Activity");
				long rowid = data.getLongExtra("ordersid", 0);
				String wb_no = data.getStringExtra("wb_no");
				String p_d_in = data.getStringExtra("p_d_in");
				String tdd = data.getStringExtra("tdd");
				String rcpn = data.getStringExtra("rcpn");
				String[] snddata = { wb_no, p_d_in, tdd, rcpn };
				Log.d("CourierMain.onActivityResult", "wb_no = " + wb_no
						+ " p_d_in = " + p_d_in + " tdd = " + tdd + " rcpn = "
						+ rcpn + " rowid = " + rowid);
				
				int sendResult = nwork.sendData(this.dbHelper, this.user, this.pwd,
				this.login_URL, this.getdata_URL, snddata);
				if (sendResult == -1) {
					// ��� ���� - ��������� ������ snddata � ������� ���������
					dbHelper.saveSnddata("SetPOD", snddata); // 6|SetPOD|1567-3118|20130603|14:54|testoffline1
				}
				
				// ���������� ������� tdd
				dbHelper.updPodTime(rowid, tdd);
				cursor.requery();
				dataAdapter.swapCursor(dbHelper.fetchModOrders());
				dataAdapter.notifyDataSetChanged();
			} else {
				Log.d("CourierMain.onActivityResult", "POD Result cancel");
			}
			break;

		default:
			Log.d("CourierMain.onActivityResult", "WARNING: undefined activity requestCode!");
			break;
		}
	}
	
	private int getNetworkData(NetWorker nwork, String user, String pwd) {
		int res = 0;
		// ��������� ������� ����������
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			
			Log.d("CourierMain.getNetworkData", "--- Network OK ---");
			
	    	// �������� ����������� ������� ���������
/*	    	SharedPreferences sharedAppConfig;
	    	sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
	    	this.login_URL = sharedAppConfig.getString(APPCFG_LOGIN_URL, "");
	    	this.getdata_URL = sharedAppConfig.getString(APPCFG_GETDATA_URL, "");
			Log.d("CourierMain.getNetworkData", "Login URL = " + login_URL + " GetData URL = " + getdata_URL);
*/	    	
		    res = nwork.getData(dbHelper, user, pwd, login_URL, getdata_URL);
		    
		    // ������������� ��������� ������
		    this.tvCourName.setText(nwork.username);
		    this.tvRefrTime.setText(this.getDateTimeEvent(1));
		    int cntrecsall = dbHelper.getCountOrd();
		    int cntnewrecs = dbHelper.getNewCountOrd();
		    //this.tvNewAllRecs.setText("0/" + Integer.toString(cntrecsall)); 
		    this.tvAllRecs.setText(Integer.toString(cntrecsall));
		    this.tvNewRecs.setText(Integer.toString(cntnewrecs));
			//dbHelper.insertTestEntries(); // DEBUG
		} else {
			// display error
			Log.d("CourierMain.getNetworkData", "--- Network Failed ---");
		}	
		return res;
	}
	
	String user, pwd, username, prev_user;
	String login_URL, getdata_URL;

	boolean checkSameUserLogin (String userLogin, String prev_login) {
		// �������� �� ���������� ���������� ����� ������������ � �����������,
		// ����� ���������� ������� ������ ������ ��� ���
		return userLogin.equals(prev_login);
	}
	
	// ���������� ���� ����� ����� � ������
	private void showLogin() {

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setCancelable(false); // ������ �������� ������� ������� �����
		alert.setTitle("������� ��� � ������");
		
		final AlertDialog alertDialog = alert.create();
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			    LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.weight = 1;
		
		LinearLayout.LayoutParams paramsEditTxt = new LinearLayout.LayoutParams(
			    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		paramsEditTxt.weight = 1;
		
		final LinearLayout ln = new LinearLayout(this);
		ln.setOrientation(LinearLayout.HORIZONTAL);
		ln.setDividerPadding(5);
		
		final LinearLayout lnf = new LinearLayout(this);
		lnf.setOrientation(LinearLayout.VERTICAL);
		lnf.setDividerPadding(5);
		
		// ����� Layout
		final LinearLayout lnv = new LinearLayout(this);
		lnv.setOrientation(LinearLayout.VERTICAL);
		lnv.addView(lnf);
		lnv.addView(ln);
		
		final EditText etUser = new EditText(this);
		final EditText etPwd = new EditText(this);
		final EditText etNetAddr = new EditText(this);
		etPwd.setHint("Password");
		etUser.setHint("User");
		etNetAddr.setHint("������� �����");
		etPwd.setLayoutParams(paramsEditTxt);
		etUser.setLayoutParams(paramsEditTxt);
		etNetAddr.setLayoutParams(paramsEditTxt);
		
		lnf.addView(etNetAddr);
		etNetAddr.setVisibility(EditText.INVISIBLE);
		
		etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // ���� ������ �����
		
		Button btnOk = new Button(this);

		btnOk.setLayoutParams(params);
		Button btnCancel = new Button(this);
		btnCancel.setLayoutParams(params);
		Button btnSet = new Button(this);
		btnSet.setLayoutParams(params);
		btnOk.setText("Ok");
		btnCancel.setText("�����");
		btnSet.setText("��������� ����");
		ln.addView(btnOk);
		ln.addView(btnCancel);
		ln.addView(btnSet);
		
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				user = etUser.getText().toString().trim();
				pwd = etPwd.getText().toString().trim();
/*				if (! checkSameUserLogin(user)) {
					// ����� �����
					//dbHelper.deleteAllOrders();
				}*/
				Log.d("CourierMain", "--- In show login Ok ---");
				//dbHelper.open();
				
				// ���������� �������� ������
				if ((etNetAddr.getVisibility() == EditText.VISIBLE)) {
					int lenAddr = etNetAddr.getText().toString().length();
					if (lenAddr > 0) {
						// ��������� ��������� ������� �����
						SharedPreferences sharedAppConfig;
						sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
						Editor ed = sharedAppConfig.edit();
						String netAddr = etNetAddr.getText().toString();
						ed.putString(APPCFG_ADDR_URL, netAddr);
						ed.putString(APPCFG_LOGIN_URL, netAddr + "/fp/cr/data/login.php");
						ed.putString(APPCFG_GETDATA_URL, netAddr + "/fp/cr/data/data.php");
						ed.commit();
						
				    	login_URL = sharedAppConfig.getString(APPCFG_LOGIN_URL, "");
				    	getdata_URL = sharedAppConfig.getString(APPCFG_GETDATA_URL, "");
						Log.d("CourierMain", "SAVE NETWORK addr = " + netAddr + " login_URL = " + login_URL + " getdata_URL = " + getdata_URL);
					}
				}
				
				if (! checkSameUserLogin(user, prev_user)) {
					// ����� ����� ���������� �� �����������, ������� ��������� ������
					Log.d("CourierMain", "LOGIN DIFFER, DELETE LOCAL DATA, OLD LOGIN = " + prev_user + " new login = " + user);
					dbHelper.deleteAllOrders();
					// � ��������� ����� ����� ��� ��������� � ��������� ������ ������ ���������
					SharedPreferences sharedAppConfig;
					sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
					Editor ed = sharedAppConfig.edit();
					ed.putString(APPCFG_LOGIN, user);
					ed.commit();
				}
				
				int netRes = getNetworkData(nwork, user, pwd);
				if (netRes >= 0) {
					
					displayListView();
					doTimerTask();
					
					alertDialog.cancel(); // ��� dismiss() ?
				} else if (netRes == -1) {
					Toast.makeText(getApplicationContext(), "������ ����",
							Toast.LENGTH_LONG).show();
				} // else if (netRes == -2) { 
				else {
					Toast.makeText(getApplicationContext(), "������������ ����� ��� ������",
							Toast.LENGTH_LONG).show();
				}
					
			}
		});
		
		// ��������� �������� ������
		btnSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SharedPreferences sharedAppConfig;
				sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
				etNetAddr.setVisibility(EditText.VISIBLE);
				etNetAddr.setText(sharedAppConfig.getString(APPCFG_ADDR_URL, ""));
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish(); // ���������� ����� ������� ��� exception
				//alert.setCancelable(true);
				// onBackPressed();
				//alertDialog.cancel();
			}
			
		});
		
		
		lnf.addView(etUser);
		lnf.addView(etPwd);
		
		alertDialog.setView(lnv);

/*
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				user = etUser.getText().toString().trim();
				pwd = etPwd.getText().toString().trim();
				if (! checkSameUserLogin(user)) {
					// ����� �����
					//dbHelper.deleteAllOrders();
				}
				Log.d("CourierMain", "--- In show login Ok ---");
				//dbHelper.open();
				int netRes = getNetworkData(nwork, user, pwd);
				if (netRes >= 0) {
					displayListView();
					doTimerTask();
				} else if (netRes == -1) {
					Toast.makeText(getApplicationContext(), "������ ����",
							Toast.LENGTH_LONG).show();
					displayListView();
					doTimerTask();
				} else if (netRes == -2) {
					Toast.makeText(getApplicationContext(), "������������ ����� ��� ������",
							Toast.LENGTH_LONG).show();
					finish(); // ������� �� ����������
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
						Toast.makeText(getApplicationContext(), "���� �������",
								Toast.LENGTH_LONG).show();
					}
				});
		
/*		alert.setNeutralButton("���������", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.d("CourierMain", "--- In SETTINGS ������ Set ---");


			}
		});*/
		
		
		alertDialog.show();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.courier_main, menu);
		return true;
	}
	

	private void displayListView() {

		cursor = dbHelper.fetchModOrders();
		
		// The desired columns to be bound
		String[] columns = new String[] {
				OrderDbAdapter.KEY_recType,
				OrderDbAdapter.KEY_OSorDNorEMP,
				OrderDbAdapter.KEY_inway,
				OrderDbAdapter.KEY_isready,
				OrderDbAdapter.KEY_aAddress,
				OrderDbAdapter.KEY_client,
				OrderDbAdapter.KEY_timeBE
		};

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.tvRecType, R.id.tvOSorDNorEmp, R.id.tvInWay, R.id.tvIsredy, 
				R.id.tvaAddress, R.id.tvClient, R.id.tvTimeBE };

		// create the adapter using the cursor pointing to the desired data
		// as well as the layout information
		dataAdapter = new MyCursorAdapter(this, R.layout.orders_info,
				cursor, columns, to, 0);
		

		listView = (ListView) findViewById(R.id.listView1);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// ������� ������ � ������
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				// Get the cursor, positioned to the corresponding row in the
				// result set
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				
				// ������ ���� ����������� �������� ������
				// listView.getChildAt(position).setBackgroundColor(Color.BLUE);
				
				// �������� �������� ���� ���� ������ �� �������
				// String ordersClient = cursor.getString(cursor
				//		.getColumnIndexOrThrow("client"));	
				
				// �������� ID ��������� � ������ ������
				ordersId = cursor.getLong(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_ROWID));
				Log.d("LISTITEMCLICK", Long.toString(ordersId) + " ������ �������������");
				
				// ���������� �������� ��������� � ������ ������
				recType_forDetail = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_recType_forDetail));
				orderDetail_aNO = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_aNo));
				tvDorder_state_ordStatus = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_ordStatus));
				tvDorder_type_ordType = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_ordType));
				tvDacash = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_aCash));
				tvDaddr_aAddress = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_aAddress));
				tvDcomp_name_client = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_client));
				tvDcontact_Cont = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_Cont));
				tvDcontact_num_ContPhone = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_ContPhone));
				tvDtimeBE = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_timeBE));
				tvDpos_num_Packs = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_Packs));
				tvDweight_Wt = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_Wt));
				tvDvol_weight_VolWt = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_VolWt));
				tvDcomment = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_Rems));
				tvLocNumItems = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_locnumitems));
				tvDIsredy = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_isready));
				tvDInway = cursor.getString(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_inway));
				
				//Toast.makeText(getApplicationContext(), ordersClient + " " + ordersId,
				//		Toast.LENGTH_SHORT).show();
				// ���������
				// dbHelper.updOrderCatchIt(ordersId, true);
				//tvNewAllRecs.setText(cursor.getCount());
				
			}
		});

/*		EditText myFilter = (EditText) findViewById(R.id.myFilter);
		myFilter.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				dataAdapter.getFilter().filter(s.toString());
			}
		});

		dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				return dbHelper.fetchOrdersByName(constraint.toString());
			}
		});*/
		
	    // ��������� ����������� ���� � ������
	    registerForContextMenu(listView);

	}
    
	// �������� ������������ ����
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_CATCH_ORDER, 0, "������ ����������");
		menu.add(0, CM_RET_ORDER, 0, "�������� ����������");
		menu.add(0, CM_BACK_ORDER, 0, "����� � ������");
	}

	@SuppressWarnings("deprecation")
	public boolean onContextItemSelected(MenuItem item) {
		// �������� �� ������ ������������ ���� ������ �� ������ ������
		AdapterContextMenuInfo order_acmi = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == CM_CATCH_ORDER) {
			// ��������� id ������ � ��������� ��������������� ������ � ��
			dbHelper.updOrderCatchIt(order_acmi.id, orderDetail_aNO);
			// ��������� ������
			cursor.requery();
			dataAdapter.notifyDataSetChanged();
			return true;
		}
		else if (item.getItemId() == CM_RET_ORDER) {
			// ��������� id ������ � ��������� ��������������� ������ � ��
			dbHelper.updOrderCatchIt(order_acmi.id, orderDetail_aNO);
			// ��������� ������
			cursor.requery();
			dataAdapter.notifyDataSetChanged();
			return true;	
		}
		return super.onContextItemSelected(item);
	}
	  
	  protected void onDestroy() {
		    super.onDestroy();
		    // ��������� ����������� ��� ������
		    stopTask();
		    dbHelper.close();
		  }

	@Override
	public void onClick(View v) {
		// ���������� ������ ������� ��������
		
		switch (v.getId()) {
		case R.id.btnAddr:
			Log.d("CourierMain", "--- In SORT ������ ����� ---");
			//dataAdapter.swapCursor(cursor).close();
			//cursor = dbHelper.fetchSortOrders(1);
			if (SQLSORTORDER == 0) {SQLSORTORDER = 1;} else SQLSORTORDER = 0;
			dataAdapter.swapCursor(dbHelper.fetchSortOrders(SQLSORTORDER));
			dataAdapter.notifyDataSetChanged();
			break;
			
		case R.id.btnClient:
			Log.d("CourierMain", "--- In SORT ������ ������ ---");
			if (SQLSORTORDER == 0) {SQLSORTORDER = 1;} else SQLSORTORDER = 0;
			dataAdapter.swapCursor(dbHelper.fetchSortClients(SQLSORTORDER));
			dataAdapter.notifyDataSetChanged();
			break;
			
		case R.id.btnTime:
			Log.d("CourierMain", "--- In SORT ������ ����� ---");
			if (SQLSORTORDER == 0) {SQLSORTORDER = 1;} else SQLSORTORDER = 0;
			dataAdapter.swapCursor(dbHelper.fetchSortTimeB(SQLSORTORDER));
			dataAdapter.notifyDataSetChanged();
			break;
			
/*		case R.id.btnSaveSet:
			Log.d("CourierMain", "--- In SETTINGS ������ Set ---");
			Intent intentActSet = new Intent(this, ActSettings.class);
			startActivity(intentActSet);
			break;*/
			
		case R.id.btnInWay:
			// ��� ������� ������ ���������� �� ������ �������������� (�.�. ������ ����� ���� ������)
			Log.d("CourierMain", "--- In switch ������ ��� ---");
			if (! tvDIsredy.equals("1")) { // ����� ������ ������ ��� ������ � ���������� ������ � ������ � ������
				int catchres = dbHelper.updOrderCatchIt(ordersId, orderDetail_aNO);
				if (catchres == 1) {
					Toast.makeText(getApplicationContext(), "���������� ������ ���",
							Toast.LENGTH_LONG).show();
				} else if (catchres == 0) {
					Toast.makeText(getApplicationContext(), "������ ��� �������",
							Toast.LENGTH_LONG).show();
				} else if (catchres == 2) {
					Toast.makeText(getApplicationContext(), "������ ��� �� ����� ���� ���������� ������ ��� � ����� ������",
							Toast.LENGTH_LONG).show();
				}
				// ��������� ������
				cursor.requery();
				dataAdapter.swapCursor(dbHelper.fetchModOrders());
				dataAdapter.notifyDataSetChanged();
				
				// �������� ������ �� ������
				if (catchres != 2) {
					Log.d("THREAD", Thread.currentThread().getName());
					Thread tInWaySender = new Thread(new Runnable() {
						public void run() {
							String[] snddata = { orderDetail_aNO, "go", getDateTimeEvent (0), "" };
							// ����� ���� ������� -1 �� nwork.sendDataGRV ���� ���� ������ ��������,
							// ��������� snddata � ��������� ����� ����� ���������� ����� ��������� ��� ��������������
							int sendResult = nwork.sendDataGRV(dbHelper, user, pwd,
									login_URL, getdata_URL, snddata);
							if (sendResult == -1) {
								// ��� ���� - ��������� ������ snddata � ������� ���������
								dbHelper.saveSnddata("courLog", snddata);
							}
							Log.d("THREAD", Thread.currentThread().getName());
						}
					});
					tInWaySender.start();
				}
			} else {
				Log.d("CourierMain", "--- ��� ������ ���������� ��� �������� ������ ---");
				Toast.makeText(getApplicationContext(), "��� ������ ���������� ��� ������� ��",
						Toast.LENGTH_LONG).show();
			}
			break;
			
		case R.id.btnOk:
			Log.d("CourierMain", "--- In switch ������ �� ordersId = " + ordersId);
			// ����� �������� ���� �������� �������� ���������� tvDIsredy (����� �� ������������� ������� ������ ��� ����������� ���������) 
			if (! recType_forDetail.equals("1")) { // �� ��������� ������ �� ������ �� ������
				if (! tvDIsredy.equals("1")) { // ���� ������ ������� ������ �� �� 
					dbHelper.updOrderIsRedy(ordersId, true);
					Toast.makeText(getApplicationContext(), "���������� ������ ��",
							Toast.LENGTH_LONG).show();
					tvDIsredy = "1";
					// ����� ��������� ������� �� ���� �������� ������ ��� - 
					// FIX �� ������������ � updOrderIsRedy
					if (! tvDInway.equals("0")) {
						//int catchres = dbHelper.updOrderCatchIt(ordersId, orderDetail_aNO);
					}
				} else {
					dbHelper.updOrderIsRedy(ordersId, false);
					Toast.makeText(getApplicationContext(), "������ �� �������",
							Toast.LENGTH_LONG).show();
					tvDIsredy = "0";
				} 
	
				// ��������� ������
				cursor.requery();
				dataAdapter.swapCursor(dbHelper.fetchModOrders());
				dataAdapter.notifyDataSetChanged();
				
				// �������� ������ �� ������				
				Log.d("THREAD", Thread.currentThread().getName());
				Thread tInOkSender = new Thread(new Runnable() {
					public void run() {
						String[] snddata = { orderDetail_aNO, "ready", getDateTimeEvent (0), "" };
						int sendResult = nwork.sendDataGRV(dbHelper, user, pwd,
								login_URL, getdata_URL, snddata);
						if (sendResult == -1) {
							// ��� ���� - ��������� ������ snddata � ������� ���������
							dbHelper.saveSnddata("courLog", snddata);
						}
						Log.d("THREAD", Thread.currentThread().getName());
					}
				});
				tInOkSender.start();
			} else {
				Toast.makeText(getApplicationContext(), "��� ��������� �� ���������",
						Toast.LENGTH_LONG).show();
			}
			break;
			
		case R.id.btnPod:
			// �������� ������ ��� ���������, recType_forDetail.equals("1")
			Log.d("CourierMain", "--- In switch ������ ��� ---");
			if (recType_forDetail.equals("1")) {
				Intent intentPOD = new Intent(this, ActPod.class);
				intentPOD.putExtra("ordersid", ordersId);
				intentPOD.putExtra("tvDorder_num", orderDetail_aNO);
				startActivityForResult(intentPOD, ARC_POD);
				// ������������ �������� �������������� � onActivityResult
			} else {
				Toast.makeText(getApplicationContext(), "��� ������� � ������ �� ���������",
						Toast.LENGTH_LONG).show();
			}
			break;
			
		case R.id.btnDetail:
			Log.d("CourierMain", "--- In switch ������ �������� --- ��� " + recType_forDetail);
			// ��������� ���� isredy
			// dbHelper.updOrderIsRedy(ordersId);
			dbHelper.updOrderIsView(ordersId);
			cursor.requery();
			dataAdapter.swapCursor(dbHelper.fetchModOrders());
			dataAdapter.notifyDataSetChanged();
			if (recType_forDetail.equals("0")) {
				// ������ ������
				Intent intent = new Intent(this, ActOrderDetail.class);
			
				intent.putExtra("tvDorder_num", orderDetail_aNO);
				intent.putExtra("tvDorder_state_ordStatus", tvDorder_state_ordStatus);
				// intent.putExtra("tvDorder_type_ordType", tvDorder_type_ordType); ���������� � ��
				intent.putExtra("tvDacash", tvDacash);
				intent.putExtra("tvDaddr_aAddress", tvDaddr_aAddress);
				intent.putExtra("tvDcomp_name_client", tvDcomp_name_client);
				intent.putExtra("tvDcontact_Cont", tvDcontact_Cont);
				intent.putExtra("tvDcontact_num_ContPhone", tvDcontact_num_ContPhone);
				intent.putExtra("tvDtimeBE", tvDtimeBE);
				intent.putExtra("tvDpos_num_Packs", tvDpos_num_Packs);
				intent.putExtra("tvDweight_Wt", tvDweight_Wt);
				intent.putExtra("tvDvol_weight_VolWt", tvDvol_weight_VolWt);
				intent.putExtra("tvDcomment", tvDcomment);
				startActivity(intent);
			} else if (recType_forDetail.equals("1")) {
				// ������ ���������
				Intent intent = new Intent(this, ActDlvDetail.class);
				
				intent.putExtra("tvDorder_num", orderDetail_aNO);
				intent.putExtra("tvDorder_state_ordStatus", tvDorder_state_ordStatus);
				intent.putExtra("tvDorder_type_ordType", tvDorder_type_ordType);
				intent.putExtra("tvDacash", tvDacash);
				intent.putExtra("tvDaddr_aAddress", tvDaddr_aAddress);
				intent.putExtra("tvDcomp_name_client", tvDcomp_name_client);
				intent.putExtra("tvDcontact_Cont", tvDcontact_Cont);
				intent.putExtra("tvDcontact_num_ContPhone", tvDcontact_num_ContPhone);
				intent.putExtra("tvDtimeBE", tvDtimeBE);
				intent.putExtra("tvDpos_num_Packs", tvDpos_num_Packs);
				intent.putExtra("tvDweight_Wt", tvDweight_Wt);
				intent.putExtra("tvDvol_weight_VolWt", tvDvol_weight_VolWt);
				intent.putExtra("tvDcomment", tvDcomment);
				startActivity(intent);
			}
			
			// �������� ������ �� ������				
			Log.d("THREAD", Thread.currentThread().getName());
			Thread tDetailSender = new Thread(new Runnable() {
				public void run() {
					String[] snddata = { orderDetail_aNO, "vieword", getDateTimeEvent (0), "" };
					int sendResult = nwork.sendDataGRV(dbHelper, user, pwd,
							login_URL, getdata_URL, snddata);
					if (sendResult == -1) {
						// ��� ���� - ��������� ������ snddata � ������� ���������
						dbHelper.saveSnddata("courLog", snddata);
					}
					
					Log.d("THREAD", Thread.currentThread().getName());
				}
			});
			tDetailSender.start();
			tvNewRecs.setText(Integer.toString(dbHelper.getNewCountOrd()));
			Log.d("DETAIL_KEY", "--- tvDorder_num = " + orderDetail_aNO);
			break;
			
		case R.id.btnNumItems:
				// ���-�� �����������
			if (ordersId != 0) {
				Intent intent = new Intent(this, ActNumItems.class);
				
				intent.putExtra("tvLocNumItems", tvLocNumItems);
				intent.putExtra("ordersid", ordersId);
				startActivityForResult(intent, ARC_NUMITEMS);
			}
			break;
		
		case R.id.btnAll:
			// �������� ��� ������ ��� �������������
			dbHelper.updAllView();
			cursor.requery();
			dataAdapter.swapCursor(dbHelper.fetchModOrders());
			dataAdapter.notifyDataSetChanged();
			tvNewRecs.setText(Integer.toString(dbHelper.getNewCountOrd()));
			Toast.makeText(getApplicationContext(), "��� ������ �������� ��� �������������",
					Toast.LENGTH_LONG).show();
			break;
			
		case R.id.btnExit:
				finish(); // ������� �� ����������
			break;
		default:
			break;
		}
		
	} // End onClick
	
	// �������������� ������ � �������
	TimerTask mTimerTask;
	final Handler handler = new Handler();
	Timer t = new Timer();
	final int TIMER_START = 3000; // �������� ����� �������� ����
	final int TIMER_PERIOD = 180000; // ������ ������� ����
	
	// ���������� ��������� �����
	public void doTimerTask() {
		
		mTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// ����� ��� ���������� �� �������
						Log.d("TIMER", "TimerTask run");
						
						// dbHelper.deleteAllOrders(); ������ �� ������ ��������� ���� �������� ����� ��������� ������ � NetWorker
						int cntnewrecs = nwork.getData(dbHelper, user, pwd, login_URL, getdata_URL);
						
						if (cntnewrecs >= 0) { // ����� � �������� ��
							imgvSrvOff.setVisibility(View.INVISIBLE);
							imgvSrvOn.setVisibility(View.VISIBLE);
							
							cursor.requery();
							int cntallrecs = cursor.getCount();
							int cntnewnotviewrecs = dbHelper.getNewCountOrd();
							//tvNewAllRecs.setText(Integer.toString(cntnewrecs) + "/" + Integer.toString(cntallrecs));
							tvAllRecs.setText(Integer.toString(cntallrecs));
							tvNewRecs.setText(Integer.toString(cntnewnotviewrecs));
							tvRefrTime.setText(getDateTimeEvent(1));
							Log.d("TIMER", "Count records from cursor new/all " + cntnewnotviewrecs + "/" + cntallrecs);
							dataAdapter.swapCursor(dbHelper.fetchModOrders());
							dataAdapter.notifyDataSetChanged();
							
							sendOfflineData(); // ���������� ������� ������
						} else { // �������� ����� � ��������
							imgvSrvOff.setVisibility(View.VISIBLE);
							imgvSrvOn.setVisibility(View.INVISIBLE);
							//tvNewAllRecs.setText("");
							tvNewRecs.setText("");
							tvAllRecs.setText("");
							tvRefrTime.setText(getDateTimeEvent(1));
						}
					}
					
				});
				
			}
		};
		
		t.schedule(mTimerTask, TIMER_START, TIMER_PERIOD);
		
	}
	
	public void stopTask() {
		if (mTimerTask != null) {
			Log.d("TIMER", "TimerTask canceled");
			mTimerTask.cancel();
		}
	}
	
	String getDateTimeEvent (int retFormat) {
		// ���������� ���� � ����� � �������
		final String DTFORMAT; // = "yyyyMMdd HH:mm";
		switch (retFormat) {
		case 0:
			DTFORMAT = "yyyyMMdd HH:mm";
			break;
		case 1:
			DTFORMAT = "HH:mm";
			break;

		default:
			DTFORMAT = "HH:mm";
			break;
		}
		Date c = Calendar.getInstance().getTime(); 
		TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				DTFORMAT);
		dateFormat.setTimeZone(tz);
		return dateFormat.format(c);
	}
	
	// �������� ������� ������ 
	void sendOfflineData() {
		final List<String[]> dataList = dbHelper.getSnddata();
		if ( dataList != null) {
					
			for (String[] rowData : dataList) {
				
				Log.d("CourierMain", "Offline send = " + rowData[0] + rowData[1] + rowData[2] + rowData[3] + " id=" + rowData[4] + " type=" + rowData[5]);
				if ((rowData[5]).equals("courLog")) {
					// String[] snddata = { orderDetail_aNO, event, tdd, "" }
					int sendResult = nwork.sendDataGRV(dbHelper, user, pwd,
							login_URL, getdata_URL, rowData);
					if (sendResult >= 0) { // �������� �� ������ �������, ������� ������� ������
						dbHelper.deleteOfflineData(rowData[4]);
					}
				} else if ((rowData[5]).equals("SetPOD")) {
					// String[] snddata = { wb_no, p_d_in, tdd, rcpn };
					int sendResult = nwork.sendData(this.dbHelper, this.user, this.pwd,
							this.login_URL, this.getdata_URL, rowData);
					if (sendResult >= 0) {// �������� �� ������ �������, ������� ������� ������
						dbHelper.deleteOfflineData(rowData[4]);
					}
				} else {
					Log.d("CourierMain", "WARNING sendOfflineData unknown type for sending=" + rowData[5]);
				}
			}
		}
	}
	
}

