package com.informixonline.courierproto;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/*
 * ������� ��� ������ ��������� ������
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) 
public class MyCursorAdapter extends SimpleCursorAdapter {
	   
	   public MyCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		inflater = LayoutInflater.from(context);
	}
	LayoutInflater inflater;

	   
	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		
		LinearLayout llvMain = (LinearLayout)view.findViewById(R.id.llvMain);
		TextView tvRecType = (TextView)view.findViewById(R.id.tvRecType);
		tvRecType.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_recType)));
		
		TextView tvOSorDNorEmp = (TextView)view.findViewById(R.id.tvOSorDNorEmp);
		String strOSorDNorEmp = cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_OSorDNorEMP));
		if (! strOSorDNorEmp.equals("NULL")) {
			tvOSorDNorEmp.setText(strOSorDNorEmp);
		} else {
			tvOSorDNorEmp.setText("");
		}
		
		TextView inWay = (TextView)view.findViewById(R.id.tvInWay);		
		if (cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_inway)).equals("���")) {
			// llvMain.setBackgroundColor(Color.rgb(255,228,181));
			inWay.setTextColor(Color.BLUE);
			inWay.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_inway)));
		} else {
			// llvMain.setBackgroundColor(Color.WHITE);
			inWay.setTextColor(Color.GRAY);
			inWay.setText("���");			
		}
		//inWay.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_inway)));
		
		// ��������� ��������� ������ � ���������
		if (CourierMain.ordersId == cursor.getLong(cursor.getColumnIndexOrThrow(OrderDbAdapter.KEY_ROWID))) {
			// ��������� ������ - �������
			llvMain.setBackgroundColor(Color.rgb(0,255,127));
		} else if (cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_isview)).equals("1")) {
			// ������������� ������ - �����
			llvMain.setBackgroundColor(Color.WHITE);
		} else {
			// ��������� �������
			llvMain.setBackgroundColor(Color.rgb(135,206,250));
		}
		

		
		TextView tvAddress = (TextView)view.findViewById(R.id.tvaAddress);
		tvAddress.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_aAddress)));
		
		TextView tvClient = (TextView)view.findViewById(R.id.tvClient);
		tvClient.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_client)));
		
		TextView tvTimeBE = (TextView)view.findViewById(R.id.tvTimeBE);
		tvTimeBE.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_timeBE)));
		
		TextView tvNumt = (TextView)view.findViewById(R.id.tvNumt);
		TextView tvNum = (TextView)view.findViewById(R.id.tvNum);
		
		// ��� 0 - �����, 1 - ���������, 2 - ����
		TextView tvIsredy = (TextView)view.findViewById(R.id.tvIsredy);
		String recType_forDetail = cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_recType_forDetail));
		tvNumt.setText("");
		tvNum.setText("");
		if (recType_forDetail.equals("0")) {
			// ������ ��� �������
			// ��������� ����� �� 
			//cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_recType_forDetail))
			if (cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_isready)).equals("1")) {
				tvIsredy.setTextColor(Color.BLUE);
				//tvIsredy.setText(cursor.getColumnIndex(OrderDbAdapter.KEY_isready));
				tvIsredy.setText("��");
			} else {
				tvIsredy.setTextColor(Color.GRAY);
				tvIsredy.setText("��");
			}
			//tvIsredy.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_isready)));
			
			tvNumt.setText(" ���-��:");
			tvNum.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_locnumitems)));
			
		} else if (recType_forDetail.equals("1")) {
			// ���������
			if (cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_tdd)).equals("null")) {
				tvIsredy.setText("���");
				tvIsredy.setTextColor(Color.BLUE);
				//tvIsredy.setText(cursor.getColumnIndex(OrderDbAdapter.KEY_isready));
			} else {
				tvIsredy.setText(cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_tdd)));
				tvIsredy.setTextColor(Color.GRAY);
			}
			
		} else if (recType_forDetail.equals("2")) {
			// ����
			// ��������� ����� �� 
			//cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_recType_forDetail))
			if (cursor.getString(cursor.getColumnIndex(OrderDbAdapter.KEY_isready)).equals("1")) {
				tvIsredy.setTextColor(Color.BLUE);
				//tvIsredy.setText(cursor.getColumnIndex(OrderDbAdapter.KEY_isready));
				tvIsredy.setText("��");
			} else {
				tvIsredy.setTextColor(Color.GRAY);
				tvIsredy.setText("��");
			}
		}
		
	}
	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
		return inflater.inflate(R.layout.orders_info, parent, false);
	}

}
