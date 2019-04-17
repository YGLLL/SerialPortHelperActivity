package com.huruwo.serialporthelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.bjw.bean.ComBean;
import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;

import java.io.IOException;

import android_serialport_api.SerialPortFinder;

public class SerialPortHelperActivity extends AppCompatActivity {

    private RecyclerView recy;
    private Spinner spSerial;
    private Spinner spInput;
    private Button btSend;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private SerialPortFinder serialPortFinder;
    private SerialHelper serialHelper;
    private Spinner spBote;
    private Button btOpen;
    private LogListAdapter logListAdapter;

    private final String[] commands = new String[]{"7E 01 00 00 07 4D 4F 54 4F 52 20 01 BF C5 7E", "7E 01 00 00 00 00 07 4D 4F 54 4F 52 20 01 BF C5 7E", "7E 02 07 52 4C 4F 43 4B 20 01 BF C5 7E", "7E 03 00 00 07 53 43 41 4E 20 4F 4E BF C5 7E"};
    //波特率
    private final String[] botes = new String[]{"0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000"};
    private String command="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seria_port_helper);

        recy = (RecyclerView) findViewById(R.id.recy);
        spSerial = (Spinner) findViewById(R.id.sp_serial);
        spInput = (Spinner) findViewById(R.id.sp_input);
        btSend = (Button) findViewById(R.id.bt_send);
        spBote = (Spinner) findViewById(R.id.sp_bote);
        btOpen = (Button) findViewById(R.id.bt_open);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);


        logListAdapter = new LogListAdapter(null);

        recy.setLayoutManager(new LinearLayoutManager(this));
        recy.setAdapter(logListAdapter);
        recy.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        iniview();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHelper.close();
    }

    private void iniview() {


        serialPortFinder = new SerialPortFinder();
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), FuncUtil.ByteArrToHex(comBean.bRec), Toast.LENGTH_SHORT).show();
                        logListAdapter.addData(comBean.sRecTime+":   "+FuncUtil.ByteArrToHex(comBean.bRec));
                        recy.smoothScrollToPosition(logListAdapter.getData().size());
                    }
                });
            }
        };

        //获取串口号
        final String[] ports = serialPortFinder.getAllDevicesPath();

        SpAdapter spAdapter = new SpAdapter(this);
        spAdapter.setDatas(ports);
        spSerial.setAdapter(spAdapter);

        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialHelper.close();
                serialHelper.setPort(ports[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter2 = new SpAdapter(this);
        spAdapter2.setDatas(botes);
        spBote.setAdapter(spAdapter2);


        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialHelper.close();
                serialHelper.setBaudRate(botes[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter3 = new SpAdapter(this);
        spAdapter3.setDatas(commands);
        spInput.setAdapter(spAdapter3);

        spInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                command=commands[position].replace(" ","");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serialHelper.open();
                    btOpen.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    //发送text
                    if (command.length() > 0) {
                        if (serialHelper.isOpen()) {
                            Toast.makeText(getBaseContext(), "发送text指令："+command, Toast.LENGTH_SHORT).show();
                            serialHelper.sendTxt(command);
                        } else {
                            Toast.makeText(getBaseContext(), "串口没打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "填数据吧", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //发送hex
                    if (command.length() > 0) {
                        if (serialHelper.isOpen()) {
                            Toast.makeText(getBaseContext(), "发送hex指令："+command, Toast.LENGTH_SHORT).show();
                            serialHelper.sendHex(command);
                        } else {
                            Toast.makeText(getBaseContext(), "串口没打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "填数据吧", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
}
