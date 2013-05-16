package com.informixonline.courierproto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActPod extends Activity implements OnClickListener {
	
	TextView tvPodOrdNum;
	EditText edtPodTime, edtPodDest;
	Button btnOk, btnCancel;
	Date c = Calendar.getInstance().getTime();
	
	@Override
	protected void onCreate(Bundle SavedInstanceState) {
		super.onCreate(SavedInstanceState);
		setContentView(R.layout.actpod);
		
		btnOk = (Button)findViewById(R.id.btnPodOk);
		btnOk.setOnClickListener(this);
		btnCancel = (Button)findViewById(R.id.btnPodCancel);
		btnCancel.setOnClickListener(this);
		
		Intent intent = getIntent();
		
		String aNo = intent.getStringExtra("tvDorder_num");
		tvPodOrdNum = (TextView)findViewById(R.id.tvPodOrdNum);
		tvPodOrdNum.setText(aNo);
		
		edtPodTime = (EditText)findViewById(R.id.edtPodTime);
		edtPodTime.setText(new SimpleDateFormat("HH:mm").format(c)); //"HHmmss"
		
		edtPodDest = (EditText)findViewById(R.id.edtPodDest);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPodOk:
			Intent intentOk = new Intent();
			intentOk.putExtra("wb_no", tvPodOrdNum.getText().toString());
			String date = new SimpleDateFormat("yyyyMMdd").format(c);
			intentOk.putExtra("p_d_in", date);
			String time = new SimpleDateFormat("HH:mm").format(c);
			intentOk.putExtra("tdd", time);
			intentOk.putExtra("rcpn", edtPodDest.getText().toString());
			
			setResult(RESULT_OK, intentOk);
			finish();
			break;
		
		case R.id.btnPodCancel:
			setResult(RESULT_CANCELED);
			finish();
			break;

		default:
			break;
		}
		
	}
}
