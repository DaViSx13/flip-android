package com.informixonline.courierproto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OrderDbAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_aNo = "aNo";
	public static final String KEY_displayNo = "displayNo";
	public static final String KEY_aCash = "aCash";
	public static final String KEY_aAddress = "aAddress";
	public static final String KEY_client = "client";
	public static final String KEY_timeB = "timeB";
	public static final String KEY_timeE = "timeE";
	public static final String KEY_tdd = "tdd";
	public static final String KEY_Cont = "Cont";
	public static final String KEY_ContPhone = "ContPhone";
	public static final String KEY_Packs = "Packs";
	public static final String KEY_Wt = "Wt";
	public static final String KEY_VolWt = "VolWt";
	public static final String KEY_Rems = "Rems";
	public static final String KEY_ordStatus = "ordStatus";
	public static final String KEY_ordType = "ordType";
	public static final String KEY_recType = "recType";
	public static final String KEY_recType_forDetail = "recType_forDetail";
	public static final String KEY_isready = "isready";
	public static final String KEY_inway = "inway";
	public static final String KEY_isview = "isview";
	public static final String KEY_rcpn = "rcpn";
	public static final String KEY_comment = "comment";

	// ���� ��� ������� � ������������
	public static final String KEY_OSorDNorEMP = "OSorDNorEMP";
	public static final String KEY_timeBE = "timeBE";
	
	// ���� ��� ���������� �������� ���-�� �������
	public static final String KEY_locnumitems = "locnumitems";
	
	static final String SQL_CASE = "select _id, " +
			"case " + 
			"when recType = '0' then '�����' " +
			"when recType = '1' then '���������' " +
			"when recType = '2' then '����' " +
		"end as recType, " +
		"case " +
			"when recType = '0' then ordStatus "+
			"when recType = '1' then displayNo "+
			"when recType = '2' then	'' "+
			"else 'Not defined' "+
		"end as OSorDNorEMP, "+
		"case "+
			"when inWay = '0' then '0' "+
			"else '���' "+
		"end as inway, "+
		"isready, "+
		"aAddress, client, "+
		"case "+
			"when recType = '0' then ' � ' || timeB || ' �� ' || timeE "+
			"else '' "+
		"end as timeBE "+
		", recType as recType_forDetail " +
		", aNo " +
		", ordStatus " +
		", ordType " +
		", aCash " +
		", Cont " +
		", ContPhone " +
		", Packs " +
		", Wt " +
		", VolWt " +
		", Rems " +
		", locnumitems " + 
		", isview " +
		"from Orders ";
	
	private static final String TAG = "OrdersDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "Courier.db";
	private static final String SQLITE_TABLE = "Orders";
	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	// ��������� �������
	private static final String DATABASE_CREATE = "CREATE TABLE if not exists "
			+ SQLITE_TABLE + " (" + KEY_ROWID
			+ " integer PRIMARY KEY autoincrement," + KEY_aNo + ","
			+ KEY_displayNo + "," + KEY_aCash + "," + KEY_aAddress + ","
			+ KEY_client + "," + KEY_timeB + "," + KEY_timeE + "," + KEY_tdd
			+ "," + KEY_Cont + "," + KEY_ContPhone + "," + KEY_Packs + ","
			+ KEY_Wt + "," + KEY_VolWt + "," + KEY_Rems + "," + KEY_ordStatus
			+ "," + KEY_ordType + "," + KEY_recType + "," + KEY_isready + ","
			+ KEY_inway + "," + KEY_isview + "," + KEY_rcpn + "," + KEY_locnumitems + " default 0 " + ");";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.w(TAG, DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
			onCreate(db);
		}
	}

	public OrderDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public OrderDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	// �������� ������ � �������
	public long createOrder(	String aNo,
								String displayNo,
								String aCash,
								String aAddress,
								String client,
								String timeB,
								String timeE,
								String tdd,
								String Cont,
								String ContPhone,
								String Packs,
								String Wt,
								String VolWt,
								String Rems,
								String ordStatus,
								String ordType,
								String recType,
								String isready,
								String inway,
								String isview,
								String rcpn) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_aNo, aNo);
		initialValues.put(KEY_displayNo, displayNo);
		initialValues.put(KEY_aCash, aCash);
		initialValues.put(KEY_aAddress, aAddress);
		initialValues.put(KEY_client, client);
		initialValues.put(KEY_timeB, timeB);
		initialValues.put(KEY_timeE, timeE);
		initialValues.put(KEY_tdd, tdd);
		initialValues.put(KEY_Cont, Cont);
		initialValues.put(KEY_ContPhone, ContPhone);
		initialValues.put(KEY_Packs, Packs);
		initialValues.put(KEY_Wt, Wt);
		initialValues.put(KEY_VolWt, VolWt);
		initialValues.put(KEY_Rems, Rems);
		initialValues.put(KEY_ordStatus, ordStatus);
		initialValues.put(KEY_ordType, ordType);
		initialValues.put(KEY_recType, recType);
		initialValues.put(KEY_isready, isready);
		initialValues.put(KEY_inway, inway);
		initialValues.put(KEY_isview, isview);
		initialValues.put(KEY_rcpn, rcpn);


		return mDb.insert(SQLITE_TABLE, null, initialValues);
	}
	
	public boolean deleteAllOrders() {

		int doneDelete = 0;
		doneDelete = mDb.delete(SQLITE_TABLE, null, null);
		Log.w(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;

	}
	
	// ���� �� �����
	public Cursor fetchOrdersByName(String inputText) throws SQLException {
		Log.w(TAG, inputText);

		return null;
	}
	
	// ��������� ��� ������ ��� �����������
	public Cursor fetchAllOrders() {

		Cursor mCursor = mDb.query(SQLITE_TABLE, new String[] { KEY_ROWID,
				KEY_aNo,
				KEY_displayNo,
				KEY_aCash,
				KEY_aAddress,
				KEY_client,
				KEY_timeB,
				KEY_timeE,
				KEY_tdd,
				KEY_Cont,
				KEY_ContPhone,
				KEY_Packs,
				KEY_Wt,
				KEY_VolWt,
				KEY_Rems,
				KEY_ordStatus,
				KEY_ordType,
				KEY_recType,
				KEY_isready,
				KEY_inway,
				KEY_isview,
				KEY_rcpn,
				KEY_locnumitems}, 
				null, null,	null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchModOrders() {
		
		Cursor mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_isview + " asc", null); // 15.05 order by isview ��� ������ ����� ������� �������
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchSortOrders(int sort) {
		Cursor mCursor = null;
		switch (sort) {
		case 0:
			Log.d("OrderDbAdapter", "--- In switch ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_aAddress + " desc", null);
			break;
		default:
			Log.d("OrderDbAdapter", "--- In switch ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_aAddress + " asc", null);
			break;
		}
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchSortClients(int sort) {
		Cursor mCursor = null;
		switch (sort) {
		case 0:
			Log.d("OrderDbAdapter", "--- In SORT ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_client + " desc", null);
			break;

		default:
			Log.d("OrderDbAdapter", "--- In SORT ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_client + " asc", null);
			break;
		}
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchSortTimeB(int sort) {
		Cursor mCursor = null;
		switch (sort) {
		case 0:
			Log.d("OrderDbAdapter", "--- In SORT ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_timeB + " desc", null);
			break;

		default:
			Log.d("OrderDbAdapter", "--- In SORT ---");
			mCursor = mDb.rawQuery(SQL_CASE + " order by " + KEY_timeB + " asc", null);
			break;
		}
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	// ������/������� ���������� ������ � ������
	int updOrderCatchIt (long rowid, String aNo) // long rowid, boolean isCatch
	{
		ContentValues cv = new ContentValues();
		int CATCH_OK = 1; // ����� ����
		int CATCH_RES = 0; // ����� �������
		int CATCHED_OTHER = 2; // ������ ������ �� �������� �����
		int res = 3;
/*		if (isCatch) {
			cv.put(KEY_inway, "1");
			Log.d(TAG, "����� ����� ����");
		} else {
			cv.put(KEY_inway, "0");
			Log.d(TAG, "����� ����� �������");
		}*/
		Cursor mCursor = mDb.rawQuery("select aNo from orders where inway = ?", new String[] {"1"});
		if (mCursor != null) {
			if (! mCursor.moveToFirst()) { // �� ������� ������ ������ � ������� ��� = 1
				cv.put(KEY_inway, "1");
				Log.d(TAG, "����� ����� ����");
				res = CATCH_OK;
				mDb.update(SQLITE_TABLE, cv, KEY_ROWID+"=?", new String [] {Long.toString(rowid)});
				Log.d(TAG, "Updated record rowid = " + rowid + " and res = " + res);
			} else { // ������� ������ � ������� ��� = 1
				String selaNo = mCursor.getString(mCursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_aNo));
				
				if (selaNo.equals(aNo)) { // ���� ��� ������� ���������� ��� = 0 ��� ���������� CATCHED_OTHER ���� �� �������
					cv.put(KEY_inway, "0");
					Log.d(TAG, "����� ����� �������");
					res = CATCH_RES;
					mDb.update(SQLITE_TABLE, cv, KEY_ROWID+"=?", new String [] {Long.toString(rowid)});
					Log.d(TAG, "Updated record rowid = " + rowid + " and res = " + res);
				} else {
					res = CATCHED_OTHER;
				}
			}
		}
		return res;
	}
	
	// ���������� ����� ��� ����� ���������� (�� ���������� � �����) (���� ��)
	int updOrderIsRedy(long rowid, boolean isready) {
		ContentValues cv = new ContentValues();
		if (isready) {
			cv.put(KEY_isready, "1"); 
			Log.d("ORDERDBADAPTER", "������ �� ����� ����������");
		} else {
			cv.put(KEY_isready, "0");
			Log.d("ORDERDBADAPTER", "������ �� ����� �������");
		}
		int rowsUpd = mDb.update(SQLITE_TABLE, cv, KEY_ROWID+"=?", new String [] {Long.toString(rowid)});
		return rowsUpd;
	}
	
	int updLocNumItems (long rowid, String numItems) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_locnumitems, numItems);
		
		int rowsUpd = mDb.update(SQLITE_TABLE, cv, KEY_ROWID+"=?", new String [] {Long.toString(rowid)});
		return rowsUpd;
	}
	
	// ���������� ���� ����� ���������� (��� ������� �� ������ ��������)
	int updOrderIsView(long rowid) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_isview, "1");
		int rowsUpd = mDb.update(SQLITE_TABLE, cv, KEY_ROWID+"=?", new String [] {Long.toString(rowid)});
		return rowsUpd;
	}
	
	// ���������� ��� ����������� ���� �� ����� ����� aNo �������� ��� ��� ����� ������ ������� ���� ��������� ��������
	boolean isNewOrder(String aNo) {
		boolean res = true; // ������ ����� �������� ���
		String SQL = "select aNo from orders where aNo = ?";
		Cursor mCursor = mDb.rawQuery(SQL, new String[] {aNo});

		if (mCursor.moveToFirst()) {
			res = false; // ����� ������ ���� ��������
		}
		Log.d("ORDERDBADAPTER", "isNewOrder record present is " + res);
		return res;
	}
	
	// �������� �������������� �� ������� �������
	boolean deleteNotExistOrd(String aNoListOnServer) {
		boolean cntDel = false;
		String SQLDEL = "delete from orders where aNo not in (" + aNoListOnServer + ")";
		Cursor mCursor = mDb.rawQuery(SQLDEL, null);
		if (mCursor.moveToFirst()) {
			cntDel = true; 
			Log.d("ORDERDBADAPTER", "record deleted");
		}
		Log.d("ORDERDBADAPTER", "record deleted " + mCursor.getCount());
		return cntDel;
	}
	
	int getCountOrd () {
		String SQLCNT = "select _id from orders";
		Cursor mCursor = mDb.rawQuery(SQLCNT, null);
		
		return mCursor.getCount();
	}

	// �������� ������
	public void insertTestEntries () {
		createOrder(
	   //aNo, displayNo, aCash, aAddress, client, timeB, timeE, tdd, Cont, ContPhone, Packs, Wt, VolWt, Rems, ordStatus, ordType, recType, isready, inway, isview, rcpn)
		"1509-9545","1509-9545","NULL","4-� ����ר����� ���. 4","���� (4-� ���)","NULL","NULL","10:15","�����","89857274131","2","2.7","0","���������","NULL","NULL","1","0","0","0","NULL");
		createOrder("3988","3988","NULL","�������� ��������, �.8","��� �� �� ��","NULL","NULL","16:42","��� �� �� ��","785-71-50","1","0.1","0","","NULL","NULL","1","0","0","0","NULL");
		createOrder("266121","�����","NULL","���������� 5-� ��. 2","������","9:00","17:30","NULL","�����","720-66-38,720-66-39","NULL","NULL","0","����: ROV -�������� ����   , ����������: ��� ��� ������� ���� +, �����:������-�� ����  ������� 117�/146 ��.12, �������: �������� ������, �������: 2462-586, ���������� �����������: ����, ���������� ����������: �� � 2555","NULL","0","0","0","0","0","NULL");
		createOrder("37965","37965","NULL","������� �.12 �� ����� 1","��������","NULL","NULL","12:25","��������","89169904999","1","0.2","0","","NULL","NULL","1","0","0","0","NULL");
		createOrder("1616-3520","1616-3520","NULL","������������� ��-�  �.63 ��.504","������� ���","NULL","NULL","12:05","�������� ����� ","9067906193","1","18.6","0","���������","NULL","NULL","1","0","1","0","NULL");
		createOrder("1619-6322","1619-6322","NULL","������������� ����� 65 ���5","����������������","NULL","NULL","16:30","����������������","7893724","2","6","0","","NULL","NULL","1","0","0","0","NULL");
		createOrder("32002431","32002431","NULL","������������� ����� �. 71 �","��� ����� ��� ��� �����","NULL","NULL","16:10","��� ����� ��� ��� ��","8-495-502-17-72","1","0.2","0","","NULL","NULL","1","0","0","0","NULL");
		createOrder("1188354","1188354","NULL","��-�.���������������  �.7�,  ����.25  ��.4", "��� �����","NULL","NULL","12:52","��� �����","����������� �������� 495-","1","0.1","0","","NULL","NULL","1","0","0","0","NULL");
		createOrder("812006317","812006317","NULL","�������� 31 �� 79","�������������","NULL","NULL","NULL","�������","9162391516","1","0.1","0","���������","NULL","NULL","1","0","0","0","NULL");
		createOrder("266130","�����","NULL","�������� ��. 14","������-���� ���","13:30","18:00","NULL","�������� .......","2312830","NULL","NULL","NULL","NULL","NULL","1","0","0","0","0","NULL");
		createOrder("1286-6456","1286-6456","NULL","�������� ��. 20�","�������� ��� ���","NULL","NULL","15:15","�������� ��������","89260012941","1","1.08","0","������ � ���������","NULL","NULL","1","0","0","0","NULL");
		createOrder("266200","�����","NULL","�������� ��. 20�","��������","NULL","NULL","NULL","����� ..�������","7887920","NULL","NULL","NULL","NULL","NULL","0","0","0","0","0","NULL");
		createOrder("1203-7452","1203-7452","NULL","��������������� ��-� �. 7 � ���. 25","����� ��� ","NULL","NULL","12:52","��������","495-641-5790","1","0.7","0","���������","NULL","NULL","1","0","0","0","NULL");
		createOrder("1366-5298","1366-5298","NULL","�� ������������� ��� 1�","����� �����","NULL","NULL","15:58","�������� �����","89250820980","1","1.26","0","����������� �����","NULL","NULL","1","0","0","0","NULL");
	}
}
