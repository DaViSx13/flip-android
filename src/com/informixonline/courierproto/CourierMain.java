package com.informixonline.courierproto;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) 
public class CourierMain extends Activity implements OnClickListener {
	
	static long ordersId; // ID ������ ���������� � ������ ��� ��������� ����� CustomAdapter
	private byte SQLSORTORDER = 0; // ���� �������� ����������: 0 desc 1 asc ��� ����������� (����� ������� ������� ������ ��������) 

	// ����� ������� �������� (������������ � ActSettings � NetWorker) ��� ������� � �������� ��������
	final static String SHAREDPREF = "sharedstore";
	final static String APPCFG_LOGIN_URL = "LOGIN_URL";
	final static String APPCFG_GETDATA_URL = "GETDATA_URL";
	
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

	// �������������� ������������ ���� ������ (��� ���������� ������� �� ������� ������)
	private static final int CM_CATCH_ORDER = 0;
	private static final int CM_RET_ORDER = 1;
	private static final int CM_BACK_ORDER = 2;
	
	private OrderDbAdapter dbHelper;
	// private SimpleCursorAdapter dataAdapter;
	private MyCursorAdapter dataAdapter;
	
	// ������ ������� ��������
	Button btnAddr, btnClient, btnTime, btnSettings, btnExit, btnInWay, btnOk, btnPod, btnDetail, btnNumItems;
	Button btnInsertTest; // ���������� ������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courier_main);
		
		// ���������� ������ ������ � ���� �����
		showLogin();
		
        // ��������� �������� ������ � ����� � ������� UI ������
        StrictMode.ThreadPolicy policy = new StrictMode.
        		ThreadPolicy.Builder().permitAll().build();
        		StrictMode.setThreadPolicy(policy);

		dbHelper = new OrderDbAdapter(this);
		dbHelper.open();

		// Clean all data
		dbHelper.deleteAllOrders();
		// Add some data
		//dbHelper.insertTestEntries();
		Log.d("POST", "--- DELETE ALL orders before connect ---");
		

		
		// ������ �������
		btnInsertTest = (Button)findViewById(R.id.btnInsertTest);
		btnInsertTest.setOnClickListener(this);
		
		// ������ �� ������� ��������
		btnAddr = (Button)findViewById(R.id.btnAddr);
		btnAddr.setOnClickListener(this);
		
		btnClient = (Button)findViewById(R.id.btnClient);
		btnClient.setOnClickListener(this);
		
		btnTime = (Button)findViewById(R.id.btnTime);
		btnTime.setOnClickListener(this);
		
		btnSettings = (Button)findViewById(R.id.btnSaveSet);
		btnSettings.setOnClickListener(this);
		
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
			} else {
				Log.d("CourierMain.onActivityResult", "Numitems Result cancel");
			}
			break;
			
		case ARC_POD:
			if (resultCode == RESULT_OK) {
				// �������� ��� (������������� ��� ���������)
				Log.d("CourierMain.onActivityResult",
						"Result from POD Activity");
				String wb_no = data.getStringExtra("wb_no");
				String p_d_in = data.getStringExtra("p_d_in");
				String tdd = data.getStringExtra("tdd");
				String rcpn = data.getStringExtra("rcpn");
				String[] snddata = { wb_no, p_d_in, tdd, rcpn };
				Log.d("CourierMain.onActivityResult", "wb_no = " + wb_no
						+ " p_d_in = " + p_d_in + " tdd = " + tdd + " rcpn = "
						+ rcpn);
				nwork.sendData(this.dbHelper, this.user, this.pwd,
				this.login_URL, this.getdata_URL, snddata);
			} else {
				Log.d("CourierMain.onActivityResult", "POD Result cancel");
			}
			break;

		default:
			Log.d("CourierMain.onActivityResult", "WARNING: undefined activity requestCode!");
			break;
		}
	}
	
	private void getNetworkData(NetWorker nwork, String user, String pwd) {
		
		// ��������� ������� ����������
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			
			Log.d("CourierMain.getNetworkData", "--- Network OK ---");
			
	    	// �������� ����������� ������� ���������
	    	SharedPreferences sharedAppConfig;
	    	sharedAppConfig = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);
	    	this.login_URL = sharedAppConfig.getString(APPCFG_LOGIN_URL, "");
	    	this.getdata_URL = sharedAppConfig.getString(APPCFG_GETDATA_URL, "");
			Log.d("CourierMain.getNetworkData", "Login URL = " + login_URL + " GetData URL = " + getdata_URL);
	    	
		    nwork.getData(dbHelper, user, pwd, login_URL, getdata_URL);
		    
			//dbHelper.insertTestEntries(); // DEBUG
		} else {
			// display error
			Log.d("CourierMain.getNetworkData", "--- Network Failed ---");
		}	
	}
	
	String user, pwd;
	String login_URL, getdata_URL;

	// ���������� ���� ����� ����� � ������
	private void showLogin() {

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("������� ��� � ������");
		
		final LinearLayout ln = new LinearLayout(this);
		ln.setOrientation(1);
		ln.setDividerPadding(5);
		final EditText etUser = new EditText(this);
		final EditText etPwd = new EditText(this);
		etPwd.setHint("Password");
		etUser.setHint("User");
		etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		ln.addView(etUser);
		ln.addView(etPwd);
		
		alert.setView(ln);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				user = etUser.getText().toString().trim();
				pwd = etPwd.getText().toString().trim();
/*				Toast.makeText(getApplicationContext(), user,
						Toast.LENGTH_SHORT).show();*/
				
				getNetworkData(nwork, user, pwd);
				displayListView();
				doTimerTask();
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
		alert.show();
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
				 
				//Toast.makeText(getApplicationContext(), ordersClient + " " + ordersId,
				//		Toast.LENGTH_SHORT).show();
				// ���������
				// dbHelper.updOrderCatchIt(ordersId, true);
				
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
			dbHelper.updOrderCatchIt(order_acmi.id, true);
			// ��������� ������
			cursor.requery();
			dataAdapter.notifyDataSetChanged();
			return true;
		}
		else if (item.getItemId() == CM_RET_ORDER) {
			// ��������� id ������ � ��������� ��������������� ������ � ��
			dbHelper.updOrderCatchIt(order_acmi.id, false);
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
			
		case R.id.btnSaveSet:
			Log.d("CourierMain", "--- In SETTINGS ������ Set ---");
			Intent intentActSet = new Intent(this, ActSettings.class);
			startActivity(intentActSet);
			break;
			
		case R.id.btnInWay:
			// ��� ������� ������ ���������� �� ������ �������������� (�.�. ������ ����� ���� ������)
			Log.d("CourierMain", "--- In switch ������ ��� ---");
			dbHelper.updOrderCatchIt(ordersId, true);
			// ��������� ������
			cursor.requery();
			dataAdapter.swapCursor(dbHelper.fetchModOrders());
			dataAdapter.notifyDataSetChanged();
			break;
			
		case R.id.btnOk:
			Log.d("CourierMain", "--- In switch ������ �� ---");
			break;
			
		case R.id.btnPod:
			// �������� ������ ��� ���������, recType_forDetail.equals("1")
			Log.d("CourierMain", "--- In switch ������ ��� ---");
			if (recType_forDetail.equals("1")) {
				Intent intentPOD = new Intent(this, ActPod.class);

				intentPOD.putExtra("tvDorder_num", orderDetail_aNO);
				startActivityForResult(intentPOD, ARC_POD);
				// ������������ �������� �������������� � onActivityResult
			}
			break;
			
		case R.id.btnDetail:
			Log.d("CourierMain", "--- In switch ������ �������� --- ��� " + recType_forDetail);
			// ��������� ���� isredy
			dbHelper.updOrderIsRedy(ordersId);
			cursor.requery();
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
			
		case R.id.btnInsertTest:
			dbHelper.insertTestEntries();
			break;
			
		case R.id.btnExit:
				finish(); // ������� �� ����������
			break;
		default:
			break;
		}
		
	} // End onClick
	
	
	TimerTask mTimerTask;
	final Handler handler = new Handler();
	Timer t = new Timer();
	final int TIMER_START = 30000;
	final int TIMER_PERIOD = 120000;
	
	public void doTimerTask() {
		
		mTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// ����� ��� ���������� �� �������
						Log.d("TIMER", "TimerTask run");
						
						dbHelper.deleteAllOrders();
						nwork.getData(dbHelper, user, pwd, login_URL, getdata_URL);
						
						cursor.requery();
						dataAdapter.swapCursor(dbHelper.fetchModOrders());
						dataAdapter.notifyDataSetChanged();
						
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
	
}

